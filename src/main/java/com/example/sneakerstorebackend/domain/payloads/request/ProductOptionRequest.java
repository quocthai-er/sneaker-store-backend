package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductOptionRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Stock is required")
    @Min(value = 1)
    private Long stock;
    @NotBlank(message = "ProductVariant is required")
    private String color;
    @NotNull(message = "Extra fee is required")
    private BigDecimal extraFee = BigDecimal.ZERO;
}
