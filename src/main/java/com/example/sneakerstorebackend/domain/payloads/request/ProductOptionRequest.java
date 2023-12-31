package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

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
    private List<MultipartFile> images;

}
