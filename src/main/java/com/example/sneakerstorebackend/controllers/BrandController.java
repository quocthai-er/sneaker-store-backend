package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.BrandConstant;
import com.example.sneakerstorebackend.service.BrandService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(BrandConstant.API_BRAND)
public class BrandController {
    private final BrandService brandService;

    @GetMapping(BrandConstant.API_FIND_ALL_BRAND)
    public ResponseEntity<?> findAll (){
        return brandService.findAll();
    }

    @GetMapping(BrandConstant.API_FIND_BRAND_BY_ID)
    public ResponseEntity<?> findBrandById (@PathVariable("id") String id){
        return brandService.findBrandById(id);
    }

}
