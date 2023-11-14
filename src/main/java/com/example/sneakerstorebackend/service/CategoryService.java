package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.CategoryRequest;
import com.example.sneakerstorebackend.entity.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface CategoryService {

    ResponseEntity<?> findCategoryById(String id);
    ResponseEntity<?> findRoot(Boolean root);
    ResponseEntity<?> addCategory(CategoryRequest req);
    ResponseEntity<?> updateCategory(String id, CategoryRequest req);
    ResponseEntity<?> findAll();

    ResponseEntity<?> deactivatedCategory(String id);


}
