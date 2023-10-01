package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Category;
import com.example.sneakerstorebackend.repository.CategoryRepository;
import com.example.sneakerstorebackend.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public ResponseEntity<?> findCategoryById(String id) {
        Optional<Category> category = categoryRepository.findCategoryByIdAndState(id, ConstantsConfig.ENABLE);
        if (category.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get category success", category));
        throw new NotFoundException("Can not found category with id: " + id);
    }

    @Override
    public ResponseEntity<?> findRoot(Boolean root) {
        List<Category> list;
        if (root) list = categoryRepository.findAllByRoot(true);
        else list = categoryRepository.findAllByState(ConstantsConfig.ENABLE);
        if (list.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all root category success", list));
        throw new NotFoundException("Can not found any category");
    }

}
