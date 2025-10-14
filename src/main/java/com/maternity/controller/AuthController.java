package com.maternity.controller;

import com.maternity.dto.AuthResponse;
import com.maternity.dto.LoginRequest;
import com.maternity.dto.PhoneSendCodeRequest;
import com.maternity.dto.PhoneSendCodeResponse;
import com.maternity.dto.PhoneVerifyRequest;
import com.maternity.dto.RegisterRequest;
import com.maternity.dto.WeChatLoginRequest;
import com.maternity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/phone/send-code")
    public ResponseEntity<PhoneSendCodeResponse> sendPhoneVerificationCode(@Valid @RequestBody PhoneSendCodeRequest request) {
        return ResponseEntity.ok(authService.sendPhoneVerificationCode(request));
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<AuthResponse> verifyPhoneAndLogin(@Valid @RequestBody PhoneVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyPhoneAndLogin(request));
    }

    @PostMapping("/wechat/login")
    public ResponseEntity<AuthResponse> loginWithWeChat(@Valid @RequestBody WeChatLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithWeChat(request));
    }
}
