package com.f2c.marketplace.controller;

import com.f2c.marketplace.dto.AuthRequest;
import com.f2c.marketplace.dto.AuthResponse;
import com.f2c.marketplace.dto.RegisterRequest;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.register(request);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        try {
            User user = userService.findByEmail(authentication.getName());
            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}
