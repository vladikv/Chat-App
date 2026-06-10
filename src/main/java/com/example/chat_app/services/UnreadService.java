package com.example.chat_app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnreadService {

    private static final String KEY_PREFIX = "unread:";
    private final RedisTemplate<String, String> redisTemplate;

    // Increment unread count for all users in room except sender
    public void increment(Long roomId, String senderUsername, java.util.Set<String> allUsernames) {
        for (String username : allUsernames) {
            if (!username.equals(senderUsername)) {
                redisTemplate.opsForHash().increment(KEY_PREFIX + username, roomId.toString(), 1);
            }
        }
    }

    // Reset unread count for user in room
    public void reset(Long roomId, String username) {
        redisTemplate.opsForHash().delete(KEY_PREFIX + username, roomId.toString());
    }

    // Get all unread counts for user { roomId -> count }
    public Map<String, Integer> getAll(String username) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(KEY_PREFIX + username);
        Map<String, Integer> result = new HashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), Integer.parseInt(v.toString())));
        return result;
    }
}