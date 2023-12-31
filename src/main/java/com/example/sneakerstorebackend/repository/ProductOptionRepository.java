package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.entity.product.ProductOption;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends MongoRepository<ProductOption, String> {
    @Query(value = "{'id': ?0, 'variants.color': ?1,}")
    Optional<ProductOption> findByIdAndVariantColor(String id, String variantColor);

    @Query(value = "{'name': ?0, 'variants.color': ?1, 'product.id': ?2}")
    Optional<ProductOption> findByNameAndVariantsColorAndProductId(String name, String colorCode, ObjectId productId);

    Optional<ProductOption> findByNameAndProduct_Id(String name, ObjectId productId);

    List<ProductOption> findAllByProduct_Id(ObjectId productId);

}
