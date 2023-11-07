package com.example.sneakerstorebackend.domain.payloads.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersSaleResponse {
    private String date;
    private BigDecimal amount;
    private int quantity;
}
