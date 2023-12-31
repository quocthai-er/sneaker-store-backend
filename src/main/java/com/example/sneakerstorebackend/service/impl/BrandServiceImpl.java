package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.CloudinaryConfig;
import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Brand;
import com.example.sneakerstorebackend.repository.BrandRepository;
import com.example.sneakerstorebackend.service.BrandService;
import com.mongodb.MongoWriteException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    private final CloudinaryConfig cloudinary;


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

    @Override
    public ResponseEntity<?> addBrand(String name, MultipartFile file) {
        String imgUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                imgUrl = cloudinary.uploadImage(file, null);
            } catch (IOException e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Error when upload image");
            }
        }
        Brand brand = new Brand(name, imgUrl , ConstantsConfig.ENABLE);
        try {
            brandRepository.save(brand);
        } catch (MongoWriteException e) {
            throw new AppException(HttpStatus.CONFLICT.value(), "Brand name already exists");
        } catch (Exception e) {
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject(true, "Create brand success", brand));
    }

    @Override
    public ResponseEntity<?> findAll(String state) {
        List<Brand> list;
        if (state == null || state.isBlank()) list=  brandRepository.findAll();
        else list=  brandRepository.findAllByState(state.toLowerCase(Locale.ROOT));
        if (list.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all brand success", list));
        throw new NotFoundException("Can not found any brand");
    }

    @Override
    public ResponseEntity<?> updateBrand(String id, String name, String state, MultipartFile file) {
        Optional<Brand> brand = brandRepository.findById(id);
        if (brand.isPresent()) {
            brand.get().setName(name);
            brand.get().setState(state);
            if (file != null && !file.isEmpty()) {
                try {
                    String imgUrl = cloudinary.uploadImage(file, brand.get().getImage());
                    brand.get().setImage(imgUrl);
                } catch (IOException e) {
                    throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Error when upload image");
                }
            }
            try {
                brandRepository.save(brand.get());
            } catch (MongoWriteException e) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Brand name already exists");
            }
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update brand success", brand));
        }
        throw new NotFoundException("Can not found brand with id: " + id);
    }

    @Override
    public ResponseEntity<?> deactivatedBrand(String id) {
        Optional<Brand> brand = brandRepository.findBrandByIdAndState(id, ConstantsConfig.ENABLE);
        if (brand.isPresent()) {
            if (!brand.get().getProducts().isEmpty()) throw new AppException(HttpStatus.CONFLICT.value(),
                    "There's a product belongs to that brand.");
            brand.get().setState(ConstantsConfig.DISABLE);
            brandRepository.save(brand.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "delete brand success with id: "+id,""));
        } else throw new NotFoundException("Can not found brand with id: " + id);
    }
}
