package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.AuthConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.*;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.AuthService;
import com.example.sneakerstorebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(AuthConstant.API_AUTH)
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtils;

    private final UserService userService;

    @PostMapping(AuthConstant.API_LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping(AuthConstant.API_REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping(AuthConstant.API_VERIFY)
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyOTPRequest verifyOTPRequest) {
        return authService.verifyOTP(verifyOTPRequest);
    }

    @PostMapping(AuthConstant.API_FORGOT_PASSWORD)
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        if (!forgotPasswordRequest.getEmail().isBlank()) return authService.forgotPassword(forgotPasswordRequest.getEmail());
        throw new AppException(HttpStatus.BAD_REQUEST.value(), "Email is required");
    }

    @PutMapping(AuthConstant.API_UPDATE_PASSWORD_RESET)
    public ResponseEntity<?> updatePasswordReset (@Valid @RequestBody ChangeResetPasswordRequest changeResetPasswordRequest,
                                                  @PathVariable("userId") String userId,
                                                  HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (user.getId().equals(userId) || !user.getId().isBlank())
            return userService.updatePasswordReset(userId, changeResetPasswordRequest);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }
}
