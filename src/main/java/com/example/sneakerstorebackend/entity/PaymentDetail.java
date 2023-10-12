package com.example.sneakerstorebackend.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {

    private String paymentId;
    private String paymentType;
    private String paymentToken;
    private Map<String, Object> paymentInfo = new HashMap<>();
}
