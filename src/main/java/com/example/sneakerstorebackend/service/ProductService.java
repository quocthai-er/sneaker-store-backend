package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ProductRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ResponseEntity<?> findAll(String state, Pageable pageable);
    ResponseEntity<?> findById(String id, String userId);
    ResponseEntity<?> findByCategoryIdOrBrandId(String id, Pageable pageable);
    ResponseEntity<?> search(String key, Pageable pageable);
    ResponseEntity<?> addProduct(ProductRequest req);
    ResponseEntity<?> updateProduct(String id, ProductRequest req);
    ResponseEntity<?> addImagesToProduct(String id, List<MultipartFile> files);

}
