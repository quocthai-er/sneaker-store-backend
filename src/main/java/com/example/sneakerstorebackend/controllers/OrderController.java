package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;
    private final JwtUtils jwtUtils;

    @GetMapping(path = "/orders/{orderId}")
    public ResponseEntity<?> userFindOrderById (@PathVariable String orderId,
                                                HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return orderService.findOrderById(orderId, user.getId());
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }
}
