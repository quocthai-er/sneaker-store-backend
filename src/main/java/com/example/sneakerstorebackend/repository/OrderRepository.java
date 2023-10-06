package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.entity.order.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findOrderByUser_IdAndState(ObjectId userId, String state);
}
