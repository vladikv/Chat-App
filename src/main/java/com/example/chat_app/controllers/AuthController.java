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

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        UserEntity user = userService.register(dto);
        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        UserEntity user = userService.findByUsername(dto.getUsername());

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getUsername()));
    }
}