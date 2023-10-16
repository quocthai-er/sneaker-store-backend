package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.OrderResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.mapper.OrderMapper;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    @Override
    public ResponseEntity<?> findOrderById(String id, String userId) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent() && order.get().getUser().getId().equals(userId)) {
            OrderResponse orderRes = orderMapper.toOrderDetailRes(order.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get order success", orderRes));
        }
        throw new NotFoundException("Can not found order with id: " + id);
    }
}
