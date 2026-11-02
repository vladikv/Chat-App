package com.example.chat_app.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client rate limiting for /api/**.
 *
 * Extends OncePerRequestFilter so it runs exactly once per request even though
 * it is a @Component (Spring Boot also auto-registers it as a servlet filter)
 * and is added to the security chain in SecurityConfig.
 * It must run AFTER JwtAuthFilter so the authenticated username is known.
 */
@Component
@Profile("!test")  // disable in test profile
public class RateLimitFilter extends OncePerRequestFilter {

    // Separate bucket maps per endpoint group
    private final Map<String, Bucket> authBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets     = new ConcurrentHashMap<>();

    private Bucket newAuthBucket() {
        // 5 requests per minute (login/register - brute force protection)
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip static resources and WebSocket
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        String ip = getClientIp(req);

        Bucket bucket;
        if (path.equals("/api/account/login") || path.equals("/api/account/register")) {
            // Key by IP - user is not authenticated yet
            bucket = authBuckets.computeIfAbsent(ip, k -> newAuthBucket());
        } else if (path.equals("/api/account/refresh")) {
            bucket = refreshBuckets.computeIfAbsent(ip, k -> newRefreshBucket());
        } else {
            // Key by username for authenticated endpoints (fall back to IP)
            String key = authenticatedUsername();
            if (key == null) key = ip;
            bucket = apiBuckets.computeIfAbsent(key, k -> newApiBucket());
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(req, res);
            return;
        }

        long retryAfter = Math.max(1,
                (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));

        res.setStatus(429);
        res.setHeader("Retry-After", String.valueOf(retryAfter));
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"error\":\"Too many requests. Please try again later.\","
                + "\"retryAfterSeconds\":" + retryAfter + "}");
    }

    private String authenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    private String getClientIp(HttpServletRequest req) {
        // NOTE: X-Forwarded-For can be spoofed by clients unless a trusted reverse
        // proxy in front of the app overwrites it.
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}