package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.ShippingConstant;
import com.example.sneakerstorebackend.domain.payloads.request.ShippingRequest;
import com.example.sneakerstorebackend.service.ShippingAPIService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(ShippingConstant.API_SHIPPING)
public class ShippingController {
    private final ShippingAPIService shippingAPIService;

    @PostMapping(path = "/fee", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> calculateFee (@RequestBody ShippingRequest req) {
        return shippingAPIService.calculateFee(req);
    }

    @PostMapping(path = "/expectedTime", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> calculateExpectedDeliveryTime (@RequestBody ShippingRequest req) {
        return shippingAPIService.calculateExpectedDeliveryTime(req);
    }

    @PostMapping(path = "/service", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getService (@RequestBody ShippingRequest req) {
        return shippingAPIService.getService(req);
    }

   /* @PostMapping(path = "/detail/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDetail (@PathVariable String orderId) {
        return shippingAPIService.getDetail(orderId);
    }*/
}