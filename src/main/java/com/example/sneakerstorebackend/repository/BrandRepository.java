package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.entity.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends MongoRepository<Brand, String> {

    List<Brand> findAllByState(String state);
    Optional<Brand> findBrandByIdAndState(String id, String state);
}
