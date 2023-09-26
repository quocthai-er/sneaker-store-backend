package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.LoginRequest;
import com.example.sneakerstorebackend.domain.payloads.request.RegisterRequest;
import com.example.sneakerstorebackend.domain.payloads.request.VerifyOTPRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> register(RegisterRequest request);

    ResponseEntity<?> forgotPassword(String email);
    ResponseEntity<?> verifyOTP(VerifyOTPRequest request);
}
