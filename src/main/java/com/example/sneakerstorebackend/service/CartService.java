package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.CartRequest;
import org.springframework.http.ResponseEntity;

public interface CartService {

    ResponseEntity<?> getProductFromCart(String userId);

    ResponseEntity<?> addAndUpdateProductToCart(String userId, CartRequest req);

    ResponseEntity<?> deleteProductFromCart(String userId, String orderItemId);

}
