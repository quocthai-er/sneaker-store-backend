package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ChangePasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.ChangeResetPasswordRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> updatePasswordReset(String id, ChangeResetPasswordRequest changeResetPasswordRequest);
}
