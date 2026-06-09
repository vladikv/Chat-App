package com.example.chat_app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private static final String ONLINE_KEY = "online_users";
    private static final Duration TTL = Duration.ofSeconds(35);

    private final RedisTemplate<String, String> redisTemplate;

    public void addUser(String username) {
        redisTemplate.opsForValue().set("online:" + username, "1", TTL);
        redisTemplate.opsForSet().add(ONLINE_KEY, username);
        System.out.println("Redis keys after add: " + redisTemplate.keys("online:*"));
    }

    public void removeUser(String username) {
        redisTemplate.delete("online:" + username);
        redisTemplate.opsForSet().remove(ONLINE_KEY, username);
    }

    public Set<String> getOnlineUsers() {
        // Filter only users whose TTL key still exists (truly online)
        Set<String> all = redisTemplate.opsForSet().members(ONLINE_KEY);
        if (all == null) return Collections.emptySet();

        Set<String> alive = ConcurrentHashMap.newKeySet();
        for (String user : all) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey("online:" + user))) {
                alive.add(user);
            } else {
                // Clean up stale entry from set
                redisTemplate.opsForSet().remove(ONLINE_KEY, user);
            }
        }
        return Collections.unmodifiableSet(alive);
    }
}