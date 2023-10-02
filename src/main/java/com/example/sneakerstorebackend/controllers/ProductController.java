package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.constant.ProductConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.ProductService;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping(ProductConstant.API_PRODUCT)
public class ProductController {
    private final ProductService productService;
    private final JwtUtils jwtUtils;

    @GetMapping(ProductConstant.API_FIND_ALL)
    public ResponseEntity<?> findAllByState (@ParameterObject Pageable pageable){
        return productService.findAll(ConstantsConfig.ENABLE, pageable);
    }


    @GetMapping(ProductConstant.API_FIND_BY_ID)
    public ResponseEntity<?> findById (@PathVariable("id") String id, HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        return productService.findById(id, user.getId());
    }

    @GetMapping(ProductConstant.API_FIND_BY_CATEGORY_ID_OR_BRAND_ID)
    public ResponseEntity<?> findByCategoryIdAndBrandId (@PathVariable("id") String id,
                                                         @ParameterObject Pageable pageable){
        return productService.findByCategoryIdOrBrandId(id, pageable);
    }


    @GetMapping(ProductConstant.API_SEARCH)
    public ResponseEntity<?> search (@RequestParam("q") String query,
                                     @PageableDefault(sort = "score") @ParameterObject Pageable pageable){
        if (query.isEmpty() || query.matches(".*[%<>&;'\0-].*"))
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid keyword");
        return productService.search(query, pageable);
    }
}