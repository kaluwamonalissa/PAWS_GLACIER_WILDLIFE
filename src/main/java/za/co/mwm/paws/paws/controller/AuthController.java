package za.co.mwm.paws.paws.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.mwm.paws.paws.dto.LoginRequest;
import za.co.mwm.paws.paws.dto.LoginResponse;
import za.co.mwm.paws.paws.dto.RegisterRequest;
import za.co.mwm.paws.paws.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody final LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody final RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestParam final String username) {
        // Stub: in production this triggers an SMS via Africa's Talking / Twilio
        return ResponseEntity.ok(
                Map.of("message", "If that username exists, a reset code has been sent via SMS."));
    }
}

