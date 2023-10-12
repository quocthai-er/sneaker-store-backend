package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.payloads.request.CheckoutRequest;
import com.example.sneakerstorebackend.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api/checkout")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(path = "/{paymentType}/{orderId}")
    public ResponseEntity<?> checkout (@PathVariable("paymentType") String paymentType,
                                       @PathVariable("orderId") String orderId,
                                       @RequestBody @Valid CheckoutRequest req,
                                       HttpServletRequest request) {
        return paymentService.createPayment(request, orderId, paymentType, req);
    }
}
