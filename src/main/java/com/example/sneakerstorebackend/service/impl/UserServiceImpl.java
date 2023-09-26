package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ChangePasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.ChangeResetPasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.user.EProvider;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public ResponseEntity<?> updatePasswordReset(String id, ChangeResetPasswordRequest ChangeResetPasswordRequest) {
        Optional<User> user = userRepository.findUserByIdAndState(id, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent() && user.get().getToken() != null) {
            if (!user.get().getProvider().equals(EProvider.LOCAL)) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.get().getProvider() + " account");
            if (ChangeResetPasswordRequest.getOTP().equals(user.get().getToken().getOtp())) {
                user.get().setPassword(passwordEncoder.encode(ChangeResetPasswordRequest.getNewPassword()));
                user.get().setToken(null);
                userRepository.save(user.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Change password success", ""));
            } else throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Your otp is wrong");
        }
        throw new NotFoundException("Can not found user with id " + id + " is activated");
    }
}
