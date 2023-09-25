package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.LoginRequest;
import com.example.sneakerstorebackend.domain.payloads.request.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> register(RegisterRequest request);

    /*ResponseEntity<?> reset(String email);
    ResponseEntity<?> verifyOTP(VerifyOTPReq req);*/
}
