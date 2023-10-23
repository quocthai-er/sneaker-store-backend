package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.CategoryRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Category;
import com.example.sneakerstorebackend.repository.CategoryRepository;
import com.example.sneakerstorebackend.service.CategoryService;
import com.mongodb.MongoWriteException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    @Override
    public ResponseEntity<?> addCategory(CategoryRequest req) {

        Category category = new Category(req.getName(), ConstantsConfig.ENABLE);
        try {
            // Add child category
            if (!req.getParent_category().equals("-1") && !req.getParent_category().isBlank()){
                Optional<Category> parentCategory = categoryRepository.findById(req.getParent_category());
                if (parentCategory.isPresent()) {
                    category.setRoot(false);
                    categoryRepository.save(category);
                    parentCategory.get().getSubCategories().add(category);
                    categoryRepository.save(parentCategory.get());
                } else throw new NotFoundException("Can not found category with id: "+req.getParent_category());
            } else categoryRepository.save(category);
        } catch (MongoWriteException e) {
            throw new AppException(HttpStatus.CONFLICT.value(), "Category name already exists");
        } catch (Exception e) {
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(true, "create category success", ""));
    }

    @Override
    public ResponseEntity<?> updateCategory(String id, CategoryRequest req) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            category.get().setName(req.getName());
            if (req.getState().isEmpty() || (!req.getState().equalsIgnoreCase(ConstantsConfig.ENABLE) &&
                    !req.getState().equalsIgnoreCase(ConstantsConfig.DISABLE)))
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid state");
            category.get().setState(req.getState());
            try {
                categoryRepository.save(category.get());
            } catch (MongoWriteException e) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Category name already exists");
            } catch (Exception e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update category success", category));
        }
        throw new NotFoundException("Can not found category with id: " + id);
    }

    @Override
    public ResponseEntity<?> findAll() {
        List<Category> list = categoryRepository.findAll();
        if (list.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all category success", list));
        throw new NotFoundException("Can not found any category");
    }

}
