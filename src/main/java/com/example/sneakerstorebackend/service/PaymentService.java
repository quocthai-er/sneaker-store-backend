package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.CheckoutRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PaymentService {
    ResponseEntity<?> createPayment(HttpServletRequest request, String id, String paymentType, CheckoutRequest req);

   ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<?> cancelPayment(String id, String responseCode, HttpServletRequest request, HttpServletResponse response);
}
