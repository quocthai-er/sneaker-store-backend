package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.entity.order.Order;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class PaymentFactory {
    public abstract ResponseEntity<?> createPayment(HttpServletRequest request, Order order);

    public abstract ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response);

}
