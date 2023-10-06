package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.entity.product.ProductOption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionRepository extends MongoRepository<ProductOption, String> {
    @Query(value = "{'id': ?0, 'variants.color': ?1,}")
    Optional<ProductOption> findByIdAndVariantColor(String id, String variantColor);
}
