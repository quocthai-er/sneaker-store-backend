package com.example.sneakerstorebackend.util;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.impl.PaymentUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
@Getter
@Setter
public class PaymentValidatorUtils implements Runnable {
    private OrderRepository orderRepository;
    private String orderId;
    private PaymentUtils paymentUtils;

    @Override
    @Async
    @Transactional
    public void run() {
        log.info("Start checking payment timeout!");
        if (!orderId.isBlank()) {
            Optional<Order> order = orderRepository.findOrderByIdAndState(orderId, ConstantsConfig.ORDER_STATE_PROCESS);
            if (order.isPresent()) {
                try {
                    if (new Date(System.currentTimeMillis() - ConstantsConfig.PAYMENT_TIMEOUT).after(
                            (Date) order.get().getPaymentDetail().getPaymentInfo()
                                    .get("orderDate"))) {
                        String check = paymentUtils.checkingUpdateQuantityProduct(order.get(), false);
                        log.info("Restock is " + (check == null));
                        order.get().setState(ConstantsConfig.ORDER_STATE_CANCEL);
                        orderRepository.save(order.get());
                        log.info("Checking payment successful!");
                    } else log.warn("Time is remaining");
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.error("failed to save order when checking payment timeout!");
                }
            }
        } else log.error("Order id in checking payment timeout is blank!");
        log.info("Checking payment timeout end!");
    }
}