package com.example.sneakerstorebackend.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface  OrderService {
    ResponseEntity<?> findOrderById(String id, String userId);
    ResponseEntity<?> cancelOrder(String id, String userId);

    ResponseEntity<?> findOrderById(String id);

    ResponseEntity<?> findAll(String state, Pageable pageable);

    ResponseEntity<?> changeState(String state, String orderId);

    ResponseEntity<?> changeState(String state, String orderId, String userId);

}
