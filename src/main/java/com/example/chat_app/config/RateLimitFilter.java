package com.example.chat_app.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    // Separate bucket maps per endpoint group
    private final Map<String, Bucket> authBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets     = new ConcurrentHashMap<>();

    private Bucket newAuthBucket() {
        // 5 requests per minute (login/register — brute force protection)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket newRefreshBucket() {
        // 10 requests per minute (refresh token)
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket newApiBucket() {
        // 60 requests per minute for all other API endpoints
        return Bucket.builder()
                .addLimit(Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse res  = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // Skip static resources and WebSocket
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip       = getClientIp(req);
        String username = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : ip;

        Bucket bucket;

        if (path.equals("/api/account/login") || path.equals("/api/account/register")) {
            // Key by IP — user is not authenticated yet
            bucket = authBuckets.computeIfAbsent(ip, k -> newAuthBucket());
        } else if (path.equals("/api/account/refresh")) {
            bucket = refreshBuckets.computeIfAbsent(ip, k -> newRefreshBucket());
        } else {
            // Key by username for authenticated endpoints
            bucket = apiBuckets.computeIfAbsent(username, k -> newApiBucket());
        }

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
        }
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}