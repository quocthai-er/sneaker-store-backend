package com.example.sneakerstorebackend.util;

import com.example.sneakerstorebackend.entity.EMailType;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.service.MailService;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
@Setter
public class CancelMailUtils implements Runnable {
    private MailService mailService;
    private Order order;

    @Override
    @SneakyThrows
    public void run() {
        Map<String, Object> model = new HashMap<>();

        model.put("userName", order.getUser().getName());
        model.put("orderID", order.getId());

        mailService.sendEmail(order.getUser().getEmail(), model, EMailType.CANCEL);
    }
}