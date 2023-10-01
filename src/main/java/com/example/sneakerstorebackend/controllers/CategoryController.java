package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.CategoryConstant;
import com.example.sneakerstorebackend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(CategoryConstant.API_CATEGORY)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(CategoryConstant.API_FIND_ROOT_CATEGORY)
    public ResponseEntity<?> findRoot (@RequestParam(value = "root", defaultValue = "true") Boolean root) {
        return categoryService.findRoot(root);
    }


    @GetMapping(CategoryConstant.API_FIND_CATEGORY_BY_ID)
    public ResponseEntity<?> findCategoryById (@PathVariable("id") String id){
        return categoryService.findCategoryById(id);
    }
}
