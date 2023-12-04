package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.service.PaymentFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl extends PaymentFactory {
    @Override
    public ResponseEntity<?> createPayment(HttpServletRequest request, Order order) {
        return null;
    }

    @Override
    public ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public ResponseEntity<?> cancelPayment(String id, String responseCode, HttpServletResponse response) {
        return null;
    }
}
