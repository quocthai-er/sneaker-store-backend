package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.OrderResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.mapper.OrderMapper;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.OrderService;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    private PaymentUtils paymentUtils;

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

    @Override
    public ResponseEntity<?> cancelOrder(String id, String userId) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent() && order.get().getUser().getId().equals(userId)) {
            if (order.get().getState().equals(ConstantsConfig.ORDER_STATE_PENDING) ||
                    order.get().getState().equals(ConstantsConfig.ORDER_STATE_PROCESS)) {
                String checkUpdateQuantityProduct = paymentUtils.checkingUpdateQuantityProduct(order.get(), false);
                order.get().setState(ConstantsConfig.ORDER_STATE_CANCEL);
                orderRepository.save(order.get());
                if (checkUpdateQuantityProduct == null) {
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new ResponseObject(true, "Cancel order successfully", ""));
                }
            } else throw new AppException(HttpStatus.BAD_REQUEST.value(),
                    "You cannot cancel while the order is still processing!");
        }
        throw new NotFoundException("Can not found order with id: " + id);
    }

    @Override
    public ResponseEntity<?> findOrderById(String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            OrderResponse orderRes = orderMapper.toOrderDetailRes(order.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get order success", orderRes));
        }
        throw new NotFoundException("Can not found order with id: " + id);
    }

    @Override
    public ResponseEntity<?> findAll(String state, Pageable pageable) {
        Page<Order> orders;
        if (state.isBlank()) orders = orderRepository.findAll(pageable);
        else orders = orderRepository.findAllByState(state, pageable);
        if (orders.isEmpty()) throw new NotFoundException("Can not found any orders");
        List<OrderResponse> resList = orders.stream().map(orderMapper::toOrderRes).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", resList);
        resp.put("totalQuantity", orders.getTotalElements());
        resp.put("totalPage", orders.getTotalPages());
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(true, "Get orders success", resp));
    }

    @Override
    public ResponseEntity<?> changeState(String state, String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            if (ConstantsConfig.ORDER_STATE_DELIVERED.equals(state)) {
                if (order.get().getState().equals(ConstantsConfig.ORDER_STATE_DELIVERY)) {
                    order.get().setState(ConstantsConfig.ORDER_STATE_DELIVERED);
                    order.get().getDeliveryDetail().getDeliveryInfo().put("deliveredAt", LocalDateTime.now(Clock.systemUTC()));
                } else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Order have not been delivering");
            } else {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid state");
            }
            orderRepository.save(order.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Change state of order successfully", state));
        } else throw new NotFoundException("Can not found order with id: " + orderId);
    }

    @Override
    public ResponseEntity<?> changeState(String state, String orderId, String userId) {
        Optional<Order> order = orderRepository.findOrderByIdAndUser_Id(orderId, new ObjectId(userId));
        if (order.isPresent()) {
            if (ConstantsConfig.ORDER_STATE_DONE.equals(state)) {
                if (order.get().getState().equals(ConstantsConfig.ORDER_STATE_DELIVERED)){
                    order.get().setState(ConstantsConfig.ORDER_STATE_DONE);
                    order.get().getPaymentDetail().getPaymentInfo().put("isPaid", true);
                }
                else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Order have not been delivered");
            } else {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid state");
            }
            orderRepository.save(order.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Finish order successfully", state));
        } else throw new NotFoundException("Can not found order with id: " + orderId);
    }
}
