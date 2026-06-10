package com.example.chat_app.services;

import com.example.chat_app.dto.account.*;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        return userRepository.save(user);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    public java.util.Set<String> getAllUsernames() {
        return userRepository.findAll().stream()
                .map(UserEntity::getUsername)
                .collect(java.util.stream.Collectors.toSet());
    }

    public UserProfileDTO getProfile(String username) {
        UserEntity user = findByUsername(username);
        return toProfileDTO(user);
    }

    public UserProfileDTO updateProfile(String username, UpdateProfileDTO dto) {
        UserEntity user = findByUsername(username);

        if (dto.getUsername() != null && !dto.getUsername().equals(username)) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already taken");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getCurrentPassword() != null && dto.getNewPassword() != null) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        return toProfileDTO(userRepository.save(user));
    }

    private UserProfileDTO toProfileDTO(UserEntity user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarColor(generateAvatarColor(user.getUsername()));
        return dto;
    }

    private String generateAvatarColor(String username) {
        String[] colors = {"#007aff", "#34c759", "#ff9500", "#af52de", "#ff2d55", "#5ac8fa"};
        int hash = username.chars().sum();
        return colors[Math.abs(hash) % colors.length];
    }
}