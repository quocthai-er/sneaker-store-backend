package com.example.sneakerstorebackend.repository;

import com.example.sneakerstorebackend.entity.order.OrderItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderItemRepository extends MongoRepository<OrderItem, String> {
}
