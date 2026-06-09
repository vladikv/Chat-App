package com.example.chat_app.controllers;

import com.example.chat_app.config.JwtService;
import com.example.chat_app.dto.account.AuthResponseDTO;
import com.example.chat_app.dto.account.LoginDTO;
import com.example.chat_app.dto.account.RegisterDTO;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.chat_app.entities.RefreshTokenEntity;
import com.example.chat_app.services.RefreshTokenService;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        UserEntity user = userService.register(dto);
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        refreshTokenService.create(user, refreshToken);
        return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshToken, user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        UserEntity user = userService.findByUsername(dto.getUsername());
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        refreshTokenService.create(user, refreshToken);
        return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshToken, user.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        if (token == null) return ResponseEntity.badRequest().build();
        try {
            RefreshTokenEntity rt = refreshTokenService.validate(token);
            String username = rt.getUser().getUsername();
            String newAccess = jwtService.generateToken(username);
            String newRefresh = jwtService.generateRefreshToken(username);
            refreshTokenService.create(rt.getUser(), newRefresh); // rotate refresh token
            return ResponseEntity.ok(new AuthResponseDTO(newAccess, newRefresh, username));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        if (token != null) refreshTokenService.revoke(token);
        return ResponseEntity.ok().build();
    }
}