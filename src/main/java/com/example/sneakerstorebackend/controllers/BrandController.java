package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.service.BrandService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class BrandController {
    private final BrandService brandService;

    @GetMapping(path = "/brands")
    public ResponseEntity<?> findAll (){
        return brandService.findAll();
    }

    @GetMapping(path = "/brands/{id}")
    public ResponseEntity<?> findBrandById (@PathVariable("id") String id){
        return brandService.findBrandById(id);
    }

}
