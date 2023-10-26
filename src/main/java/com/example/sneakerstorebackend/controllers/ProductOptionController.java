package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.payloads.request.ProductOptionRequest;
import com.example.sneakerstorebackend.service.ProductOptionService;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class ProductOptionController {
    private ProductOptionService productOptionService;

    @PostMapping(value = "/manage/products/option/{productId}")
    public ResponseEntity<?> addOption(@PathVariable("productId") String id,
                                       @RequestBody @Valid ProductOptionRequest req) {
        return productOptionService.addOption(id, req);
    }
}
