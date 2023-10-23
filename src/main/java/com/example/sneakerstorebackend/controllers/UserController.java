package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.UserConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.ChangePasswordRequest;
import com.example.sneakerstorebackend.domain.payloads.request.UserRequest;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(UserConstant.API_USER)
public class UserController {
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @GetMapping(UserConstant.API_FIND_USER_BY_ID)
    public ResponseEntity<?> findUserById (@PathVariable("userId") String userId,
                                           HttpServletRequest request) {
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (user.getId().equals(userId) || !user.getId().isBlank())
            return userService.findUserById(userId);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @PutMapping(UserConstant.API_UPDATE_USER)
    public ResponseEntity<?> updateUser (@Valid @RequestBody UserRequest userRequest,
                                         @PathVariable("userId") String userId,
                                         HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        userRequest.setState(null);
        if (user.getId().equals(userId) || !user.getId().isBlank())
            return userService.updateUser(userId, userRequest);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @PutMapping(UserConstant.API_UPDATE_PASSWORD)
    public ResponseEntity<?> updatePasswordUser (@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                                 @PathVariable("userId") String userId,
                                                 HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (user.getId().equals(userId) || !user.getId().isBlank())
            return userService.updatePassword(userId, changePasswordRequest);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @GetMapping(UserConstant.API_LIST_HISTORY_ORDER)
    public ResponseEntity<?> getUserOrderHistory (HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return userService.getUserOrderHistory(user.getId());
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

}
