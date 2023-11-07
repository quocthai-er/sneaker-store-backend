package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.domain.payloads.StateCountAggregate;
import com.example.sneakerstorebackend.entity.Category;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findAllByState(String state);

    Optional<Category> findCategoryByIdAndState(String id, String state);

    List<Category> findAllByRoot(boolean isRoot);

    @Aggregation("{ $group: { _id : $state, count: { $sum: 1 } } }")
    List<StateCountAggregate> countAllByState();
}
