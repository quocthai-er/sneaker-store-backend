package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.entity.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface CategoryService {

    ResponseEntity<?> findCategoryById(String id);
    ResponseEntity<?> findRoot(Boolean root);

}
