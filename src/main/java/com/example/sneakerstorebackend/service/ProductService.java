package com.example.sneakerstorebackend.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    ResponseEntity<?> findAll(String state, Pageable pageable);
    ResponseEntity<?> findById(String id, String userId);
    ResponseEntity<?> findByCategoryIdOrBrandId(String id, Pageable pageable);
    ResponseEntity<?> search(String key, Pageable pageable);

}
