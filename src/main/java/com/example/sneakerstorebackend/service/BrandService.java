package com.example.sneakerstorebackend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface BrandService {
    ResponseEntity<?> findAll();
    ResponseEntity<?> findBrandById(String id);

}
