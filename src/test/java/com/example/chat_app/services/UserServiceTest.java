package com.example.chat_app.services;

import com.example.chat_app.dto.account.RegisterDTO;
import com.example.chat_app.dto.account.UpdateProfileDTO;
import com.example.chat_app.dto.account.UserProfileDTO;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void register_shouldRegisterSuccessfully() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@test.com");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserEntity result = userService.register(dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void register_shouldThrowWhenUsernameTaken() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@test.com");
        dto.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void register_shouldThrowWhenEmailTaken() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setEmail("test@test.com");
        dto.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void findByUsername_shouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserEntity result = userService.findByUsername("testuser");

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_shouldThrowWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getProfile_shouldReturnProfileDTO() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserProfileDTO profile = userService.getProfile("testuser");

        assertThat(profile.getUsername()).isEqualTo("testuser");
        assertThat(profile.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void updateProfile_shouldUpdateUsernameSuccessfully() {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setUsername("newname");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        UserProfileDTO result = userService.updateProfile("testuser", dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void updateProfile_shouldThrowWhenUsernameTaken() {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setUsername("taken");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("testuser", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already taken");
    }

    @Test
    void updateProfile_shouldThrowWhenPasswordIncorrect() {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setCurrentPassword("wrong");
        dto.setNewPassword("newpass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateProfile("testuser", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    void updateAvatar_shouldUpdateAvatarUrl() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserProfileDTO result = userService.updateAvatar("testuser", "/uploads/avatar.jpg");

        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> "/uploads/avatar.jpg".equals(u.getAvatarUrl())));
    }

    @Test
    void getAllUsernames_shouldReturnAllUsernames() {
        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        var result = userService.getAllUsernames();

        assertThat(result).containsExactlyInAnyOrder("testuser", "user2");
    }
}