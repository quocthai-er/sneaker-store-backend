package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.payloads.request.ProductOptionRequest;
import com.example.sneakerstorebackend.service.ProductOptionService;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class ProductOptionController {
    private ProductOptionService productOptionService;

    @PostMapping(value = "/manage/products/option/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addOption(@PathVariable("productId") String id,
                                       @Valid @ModelAttribute ProductOptionRequest req) {
        return productOptionService.addOption(id, req);
    }

    @PutMapping(value = "/manage/products/option/{id}")
    public ResponseEntity<?> updateOptionVariant(@PathVariable("id") String id,
                                                 @RequestParam("variantColor") String variantColor,
                                                 @Valid @RequestBody ProductOptionRequest req) {
        variantColor = "#"+variantColor;
        return productOptionService.updateOptionVariant(id, variantColor, req);
    }

    @GetMapping("/products/option")
    public ResponseEntity<?> findOptionByProductId(@RequestParam("productId") String id) {
        return productOptionService.findOptionByProductId(id);
    }

    @GetMapping("/products/option/{id}")
    public ResponseEntity<?> getOptionById(@PathVariable("id") String id) {
        return productOptionService.findOptionById(id);
    }

}
