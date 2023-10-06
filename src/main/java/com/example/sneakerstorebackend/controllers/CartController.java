package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.CartConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.CartRequest;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(CartConstant.API_CART)

public class CartController {
    private final CartService cartService;
    private final JwtUtils jwtUtils;

    @GetMapping(CartConstant.API_GET_CART)
    public ResponseEntity<?> getProductFromCart (HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return cartService.getProductFromCart(user.getId());
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @PostMapping(CartConstant.API_ADD_ITEM_TO_CART)
    public ResponseEntity<?> addAndUpdateProduct (@RequestBody @Valid CartRequest req,
                                                  HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return cartService.addAndUpdateProductToCart(user.getId(), req);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @DeleteMapping(CartConstant.API_DELETE_ITEM_FROM_CART)
    public ResponseEntity<?> deleteProductInCart (@PathVariable("orderItemId") String orderItemId,
                                                  HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return cartService.deleteProductFromCart(user.getId(), orderItemId);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }
}
