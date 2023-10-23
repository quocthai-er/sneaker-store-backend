package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.constant.BrandConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.service.BrandService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @PostMapping(path = "/admin/manage/brands", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addBrand (@RequestParam(value = "name") String name,
                                       @RequestParam(value = "file",required = false) MultipartFile file){
        if (name == null || name.isBlank()) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Name is required");
        return brandService.addBrand(name, file);
    }

    @GetMapping(path = "/admin/manage/brands")
    public ResponseEntity<?> findAll (@RequestParam(value = "state", defaultValue = "") String state){
        return brandService.findAll(state);
    }

    @PostMapping(path = "/admin/manage/brands/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBrand (@PathVariable("id") String id,
                                          @RequestParam("name") String name,
                                          @RequestParam("state") String state,
                                          @RequestParam(value = "file",required = false) MultipartFile file) {
        if (name == null || name.isBlank()) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Name is required");
        if (state == null || state.isBlank() || (!state.equalsIgnoreCase(ConstantsConfig.ENABLE) &&
                !state.equalsIgnoreCase(ConstantsConfig.DISABLE)))
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "State is invalid");
        return brandService.updateBrand(id, name, state, file);
    }

}
