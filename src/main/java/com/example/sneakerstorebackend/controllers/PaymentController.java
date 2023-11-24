package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.payloads.request.CheckoutRequest;
import com.example.sneakerstorebackend.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @GetMapping("/{paymentType}/success")
    public ResponseEntity<?> successPay(@RequestParam(value = "paymentId", required = false) String paymentId,
                                        @RequestParam(value = "PayerID", required = false) String payerId,
                                        @RequestParam(value = "vnp_ResponseCode", required = false) String responseCode,
                                        @RequestParam(value = "vnp_OrderInfo", required = false) String id,
                                        @PathVariable("paymentType") String paymentType,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        switch (paymentType) {
            case ConstantsConfig.PAYMENT_PAYPAL:
                return paymentService.executePayment(paymentId,payerId,null,null, request, response);
            default:
                return paymentService.executePayment(paymentId, null,null,null, request, response);
        }
    }

    @GetMapping("/{paymentType}/cancel")
    public ResponseEntity<?> cancelPay(@RequestParam(value = "paymentId", required = false) String paymentId,
                                       @RequestParam(value = "token", required = false) String token,
                                       @PathVariable("paymentType") String paymentType,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        if (ConstantsConfig.PAYMENT_PAYPAL.equals(paymentType)) {
            return paymentService.cancelPayment(token, null, request, response);
        } else {
            return paymentService.cancelPayment(paymentId, null, request, response);
        }
    }
}
