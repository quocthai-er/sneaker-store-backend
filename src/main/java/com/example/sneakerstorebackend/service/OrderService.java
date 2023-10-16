package com.example.sneakerstorebackend.service;

import org.springframework.http.ResponseEntity;

public interface  OrderService {
    ResponseEntity<?> findOrderById(String id, String userId);
}
