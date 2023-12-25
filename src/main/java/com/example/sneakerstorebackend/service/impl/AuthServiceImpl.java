package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.VerifyOTPRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.EMailType;
import com.example.sneakerstorebackend.entity.user.EProvider;
import com.example.sneakerstorebackend.entity.user.Token;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.mapper.UserMapper;
import com.example.sneakerstorebackend.domain.payloads.request.LoginRequest;
import com.example.sneakerstorebackend.domain.payloads.request.RegisterRequest;
import com.example.sneakerstorebackend.domain.payloads.response.LoginResponse;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.security.user.CustomUserDetails;
import com.example.sneakerstorebackend.service.AuthService;
import com.example.sneakerstorebackend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    private final MailService mailService;


    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            if (user.getUser().getProvider().equals(EProvider.LOCAL) && user.getUser().getState().equals(ConstantsConfig.USER_STATE_ACTIVATED)) {
                LoginResponse loginResponse = userMapper.toLoginRes(user.getUser());
/*                if (user.getUser().getState().equals(ConstantsConfig.USER_STATE_UNVERIFIED)) {
                    try {
                        loginResponse.setAccessToken(ConstantsConfig.USER_STATE_UNVERIFIED);

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
                    }
                } else {
                    String access_token = jwtUtils.generateTokenFromUserId(user.getUser());
                    loginResponse.setAccessToken(access_token);
                }*/

                if (user.getUser().getState().equals(ConstantsConfig.USER_STATE_UNVERIFIED)) {
                    try {
                        if (loginRequest.getOtp() == null || loginRequest.getOtp().isBlank()) { sendVerifyMail(user.getUser());
                            loginResponse.setAccessToken(ConstantsConfig.USER_STATE_UNVERIFIED);
                        } else {
                            boolean verify = false;
                            if (LocalDateTime.now().isBefore(user.getUser().getToken().getExp())) {
                                if (user.getUser().getToken().getOtp().equals(loginRequest.getOtp())) {
                                    loginResponse.setAccessToken(jwtUtils.generateTokenFromUserId(user.getUser()));
                                    user.getUser().setState(ConstantsConfig.USER_STATE_ACTIVATED);
                                    userRepository.save(user.getUser());
                                    verify = true;
                                }
                                return ResponseEntity.status(HttpStatus.OK).body(
                                        new ResponseObject(verify, "OTP with email: " + loginRequest.getUsername() + " is " + verify, loginRequest));
                            } else {
                                user.getUser().setToken(null);
                                userRepository.save(user.getUser());
                                return ResponseEntity.status(HttpStatus.OK).body(
                                        new ResponseObject(false, "Expired" , ""));
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
                    }
                } else {
                    String access_token = jwtUtils.generateTokenFromUserId(user.getUser());
                    loginResponse.setAccessToken(access_token);
                }

                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Log in successfully ", loginResponse)
                );
            } else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.getUser().getProvider() + " account");
        } catch (BadCredentialsException ex) {
//            ex.printStackTrace();
            throw new BadCredentialsException(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(HttpStatus.CONFLICT.value(), "Email already exists");

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User user = userMapper.toUser(request);
        if (user != null) {
            try {
                sendVerifyMail(user);
            } catch (Exception e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(true, "Register successfully ", "")
        );
    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        Optional<User> user = userRepository.findUserByEmailAndState(email, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            if (user.get().getProvider().equals(EProvider.LOCAL)) {
                try {
                    sendVerifyMail(user.get());
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new ResponseObject(true, "Password reset request successful", email));
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                    throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Password reset request failed");
                }
            } else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.get().getProvider() + " account");
        }
        throw new NotFoundException("Unable to find user with email " + email + " is activated");
    }

    @Override
    public ResponseEntity<?> verifyOTP(VerifyOTPRequest verifyOTPRequest) {
        switch (verifyOTPRequest.getType().toLowerCase()) {
            case "register":
                return verifyRegister(verifyOTPRequest.getEmail(), verifyOTPRequest.getOtp());
            case "reset":
                return verifyReset(verifyOTPRequest.getEmail(), verifyOTPRequest.getOtp());
            default:
                throw new NotFoundException("Can not found type of verify");
        }
    }

    private ResponseEntity<?> verifyReset(String email, String otp) {
        Optional<User> user = userRepository.findUserByEmailAndState(email, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            if (!user.get().getProvider().equals(EProvider.LOCAL)) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Your account is " +
                    user.get().getProvider() + " account");
            Map<String, Object> res = new HashMap<>();
            boolean verify = false;
            if (LocalDateTime.now().isBefore(user.get().getToken().getExp())) {
                if (user.get().getToken().getOtp().equals(otp)) {
                    res.put("id", user.get().getId());
                    res.put("token", jwtUtils.generateTokenFromUserId(user.get()));
                    user.get().setPassword(user.get().getToken().getOtp());
                    userRepository.save(user.get());
                    verify = true;
                }
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(verify, "OTP with email: " + email + " is " + verify, res));
            } else {
                user.get().setToken(null);
                userRepository.save(user.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(false, "OTP with email: " + email + " is expired" , ""));
            }
        }
        throw new NotFoundException("Unable to find user with email " + email + " is activated");
    }

    private ResponseEntity<?> verifyRegister(String email, String otp) {
        Optional<User> user = userRepository.findUserByEmailAndState(email, ConstantsConfig.USER_STATE_UNVERIFIED);
        if (user.isPresent()) {
            boolean verify = false;
            if (LocalDateTime.now().isBefore(user.get().getToken().getExp())) {
                if (user.get().getToken().getOtp().equals(otp)) {
                    user.get().setState(ConstantsConfig.USER_STATE_ACTIVATED);
                    user.get().setToken(null);
                    userRepository.save(user.get());
                    verify = true;
                }
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(verify, "OTP with email: " + email + " is " + verify, ""));
            } else {
                user.get().setToken(null);
                userRepository.save(user.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(false, "OTP with email: " + email + " is expired" , ""));
            }
        }
        throw new NotFoundException("Can not found user with email " + email);
    }

    @SneakyThrows
    public void sendVerifyMail(User user) {
        String token = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        Map<String, Object> model = new HashMap<>();
        model.put("token", token);
        user.setToken(new Token(token, LocalDateTime.now().plusMinutes(5)));
        userRepository.save(user);
        mailService.sendEmail(user.getEmail(), model, EMailType.AUTH);
    }
}
