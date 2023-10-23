package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.CategoryConstant;
import com.example.sneakerstorebackend.domain.payloads.request.CategoryRequest;
import com.example.sneakerstorebackend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @GetMapping(path = "/admin/manage/categories")
    public ResponseEntity<?> findAll (){
        return categoryService.findAll();
    }

    @PostMapping(path = "/admin/manage/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addCategory (@ModelAttribute @Valid CategoryRequest req){
        return categoryService.addCategory(req);
    }

    @PutMapping(path = "/admin/manage/categories/{id}")
    public ResponseEntity<?> updateCategory (@PathVariable("id") String id,
                                             @RequestBody @Valid CategoryRequest req){
        return categoryService.updateCategory(id, req);
}
}
