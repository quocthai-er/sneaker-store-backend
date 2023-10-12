package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.MailService;
import com.example.sneakerstorebackend.service.PaymentFactory;
import com.example.sneakerstorebackend.util.MailUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;
@Service
@AllArgsConstructor
public class CODServiceImpl extends PaymentFactory {
    private PaymentUtils paymentUtils;
    private final OrderRepository orderRepository;
    private final TaskScheduler taskScheduler;
    private final MailUtils mailUtils;
    private final MailService mailService;

    @Override
    @Transactional
    public ResponseEntity<?> createPayment(HttpServletRequest request, Order order) {
        if (order != null && order.getState().equals(ConstantsConfig.ORDER_STATE_PROCESS)) {
            String checkUpdateQuantityProduct = paymentUtils.checkingUpdateQuantityProduct(order, true);
            if (checkUpdateQuantityProduct == null) {
                order.setState(ConstantsConfig.ORDER_STATE_PENDING);
                order.getPaymentDetail().getPaymentInfo().put("isPaid", false);
                orderRepository.save(order);
                mailUtils.setOrder(order);
                mailUtils.setMailService(mailService);
                taskScheduler.schedule(mailUtils, new Date(System.currentTimeMillis())) ;
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, " Pay by COD successfully", ""));
            }
        } throw new NotFoundException("Can not found order with id: "+ Objects.requireNonNull(order).getId());
    }
}
