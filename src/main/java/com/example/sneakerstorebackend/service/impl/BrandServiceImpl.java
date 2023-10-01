package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Brand;
import com.example.sneakerstorebackend.repository.BrandRepository;
import com.example.sneakerstorebackend.service.BrandService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    @Override
    public ResponseEntity<?> findAll() {
        List<Brand> list = brandRepository.findAllByState(ConstantsConfig.ENABLE);
        if (list.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all brand success", list));
        throw new NotFoundException("Can not found any brand");
    }

    @Override
    public ResponseEntity<?> findBrandById(String id) {
        Optional<Brand> brand = brandRepository.findBrandByIdAndState(id, ConstantsConfig.ENABLE);
        if (brand.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get brand success", brand));
        throw new NotFoundException("Can not found brand with id: " + id);
    }
}
