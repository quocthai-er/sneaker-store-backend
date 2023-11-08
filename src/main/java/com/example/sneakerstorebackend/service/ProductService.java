package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ProductPriceAndDiscount;
import com.example.sneakerstorebackend.domain.payloads.request.ProductRequest;
import com.example.sneakerstorebackend.entity.product.ProductAttribute;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ResponseEntity<?> findAll(String state, Pageable pageable);
    ResponseEntity<?> findById(String id, String userId);
    ResponseEntity<?> findByCategoryIdOrBrandId(String id, Pageable pageable);
    ResponseEntity<?> search(String key, Pageable pageable);
    ResponseEntity<?> addProduct(ProductRequest request);
    ResponseEntity<?> updateProduct(String id, ProductRequest request);
    ResponseEntity<?> addImagesToProduct(String id, List<MultipartFile> files);

    ResponseEntity<?> addAttribute(String id, ProductAttribute request);

    ResponseEntity<?> updateAttribute(String id, String oldName, ProductAttribute request);

    ResponseEntity<?> updateMultiplePriceAndDiscount(ProductPriceAndDiscount request);

    ResponseEntity<?> updatePriceAndDiscount(ProductPriceAndDiscount request);

    ResponseEntity<?> deleteAttribute(String id, String name);

}
