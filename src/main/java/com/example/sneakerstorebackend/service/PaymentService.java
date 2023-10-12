package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.CheckoutRequest;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface PaymentService {
    ResponseEntity<?> createPayment(HttpServletRequest request, String id, String paymentType, CheckoutRequest req);
}
