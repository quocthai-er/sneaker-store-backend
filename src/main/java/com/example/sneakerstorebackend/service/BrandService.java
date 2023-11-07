package com.example.sneakerstorebackend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface BrandService {
    ResponseEntity<?> findAll();
    ResponseEntity<?> findBrandById(String id);

    ResponseEntity<?> addBrand(String name, MultipartFile file);

    ResponseEntity<?> findAll(String state);

    ResponseEntity<?> updateBrand(String id, String name, String state, MultipartFile file);

    ResponseEntity<?> deactivatedBrand(String id);

}
