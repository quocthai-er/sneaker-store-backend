package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ProductListRespone;
import com.example.sneakerstorebackend.domain.payloads.response.ProductRespone;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Category;
import com.example.sneakerstorebackend.entity.product.Product;
import com.example.sneakerstorebackend.mapper.ProductMapper;
import com.example.sneakerstorebackend.repository.CategoryRepository;
import com.example.sneakerstorebackend.repository.ProductRepository;
import com.example.sneakerstorebackend.service.ProductService;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final CategoryRepository categoryRepository;

    @Override
    public ResponseEntity<?> findAll(String state, Pageable pageable) {
        Page<Product> products;
        if (state.equalsIgnoreCase(ConstantsConfig.ENABLE) || state.equalsIgnoreCase(ConstantsConfig.DISABLE))
            products = productRepository.findAllByState(state.toLowerCase(), pageable);
        else products = productRepository.findAll(pageable);
        List<ProductListRespone> resList = products.getContent().stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product");
    }

    private ResponseEntity<?> addPageableToRes(Page<Product> products, List<ProductListRespone> resList) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", resList);
        resp.put("totalQuantity", products.getTotalElements());
        resp.put("totalPage", products.getTotalPages());
        if (!resList.isEmpty() )
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all product success", resp));
        return null;
    }

    @Override
    public ResponseEntity<?> findById(String id, String userId) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent()) {
            ProductRespone res = productMapper.toProductRes(product.get());
           /* recommendCheckUtils.setCatId(res.getCategory());
            recommendCheckUtils.setBrandId(res.getBrand());
            recommendCheckUtils.setType(ConstantsConfig.VIEW_TYPE);
            recommendCheckUtils.setUserId(userId);
            recommendCheckUtils.setUserRepository(userRepository);
            taskScheduler.schedule(recommendCheckUtils, new Date(System.currentTimeMillis()));*/
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get product success", res));
        }
        throw new NotFoundException("Can not found any product with id: "+id);
    }

    @Override
    public ResponseEntity<?> findByCategoryIdOrBrandId(String id, Pageable pageable) {
        Page<Product> products;
        try {
            Optional<Category> category = categoryRepository.findCategoryByIdAndState(id, ConstantsConfig.ENABLE);
            if (category.isPresent()) {
                List<ObjectId> subCat = category.get().getSubCategories().stream().map(c -> new ObjectId(c.getId())).collect(Collectors.toList());
                products = productRepository.findProductsByCategory(new ObjectId(id),
                        subCat, pageable);
            } else products = productRepository.findAllByCategory_IdOrBrand_IdAndState(new ObjectId(id),
                    new ObjectId(id),ConstantsConfig.ENABLE, pageable);
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error when finding");
        }
        List<ProductListRespone> resList = products.stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product with category or brand id: "+id);
    }

    @Override
    public ResponseEntity<?> search(String key, Pageable pageable) {
        Page<Product> products;
        try {
            products = productRepository.findAllBy(TextCriteria
                            .forDefaultLanguage().matchingAny(key),
                    pageable);
        } catch (Exception e) {
            throw new NotFoundException("Can not found any product with: "+key);
        }
        List<ProductListRespone> resList = products.getContent().stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product with: "+key);
    }
}
