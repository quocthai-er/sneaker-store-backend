package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.domain.payloads.StateCountAggregate;
import com.example.sneakerstorebackend.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByEmailAndState(String email, String state);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByIdAndState(String id, String state);
    Page<User> findAllByState(String state, Pageable pageable);
    boolean existsByEmail(String email);

    @Aggregation("{ $group: { _id : $state, count: { $sum: 1 } } }")
    List<StateCountAggregate> countAllByState();
}
