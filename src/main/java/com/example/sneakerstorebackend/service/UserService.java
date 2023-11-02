package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ChangePasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.ChangeResetPasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.UserRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> updatePasswordReset(String id, ChangeResetPasswordRequest changeResetPasswordRequest);

    ResponseEntity<?> findUserById(String id);

    ResponseEntity<?> updateUser(String id, UserRequest userRequest);

    ResponseEntity<?> updatePassword(String id, ChangePasswordRequest changePasswordRequest);

    ResponseEntity<?> getUserOrderHistory(String id);

    ResponseEntity<?> findAll(String state, Pageable pageable);

}
