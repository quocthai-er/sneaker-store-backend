package com.example.sneakerstorebackend.mapper;

import com.example.sneakerstorebackend.domain.payloads.response.OrderResponse;
import com.example.sneakerstorebackend.entity.order.Order;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class OrderMapper {

    public OrderResponse toOrderRes (Order order) {
        Object orderDate = order.getLastModifiedDate();
        if (order.getPaymentDetail().getPaymentInfo().get("orderDate") != null) orderDate = order.getPaymentDetail().getPaymentInfo().get("orderDate");
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getName(),
                order.getTotalProduct(), order.getTotalPrice(), order.getState(), orderDate);
    }

    public OrderResponse toOrderDetailRes (Order order) {
        OrderResponse orderRes =  new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getName(),
                order.getTotalProduct(), order.getTotalPrice(), order.getState(), order.getLastModifiedDate());
        if (order.getPaymentDetail().getPaymentInfo().get("orderDate") != null) orderRes.setCreatedDate(order.getPaymentDetail().getPaymentInfo().get("orderDate"));
        orderRes.setItems(order.getItems().stream().map(CartMapper::toCartItemRes).collect(Collectors.toList()));
        orderRes.setPaymentType(order.getPaymentDetail().getPaymentType());
        orderRes.setPaymentInfo(order.getPaymentDetail().getPaymentInfo());
        orderRes.setDeliveryDetail(order.getDeliveryDetail());
        return orderRes;
    }
}
