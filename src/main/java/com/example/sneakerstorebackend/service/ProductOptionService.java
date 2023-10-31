package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ProductOptionRequest;
import org.springframework.http.ResponseEntity;

public interface ProductOptionService {
    ResponseEntity<?> addOption(String productId , ProductOptionRequest req);

    ResponseEntity<?> updateOptionVariant(String id, String variantColor, ProductOptionRequest req);

}

