package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.domain.payloads.StateCountAggregate;
import com.example.sneakerstorebackend.entity.Brand;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends MongoRepository<Brand, String> {

    List<Brand> findAllByState(String state);
    Optional<Brand> findBrandByIdAndState(String id, String state);

    @Aggregation("{ $group: { _id : $state, count: { $sum: 1 } } }")
    List<StateCountAggregate> countAllByState();
}
