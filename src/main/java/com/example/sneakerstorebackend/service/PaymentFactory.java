package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.entity.order.Order;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public abstract class PaymentFactory {
    public abstract ResponseEntity<?> createPayment(HttpServletRequest request, Order order);
}
