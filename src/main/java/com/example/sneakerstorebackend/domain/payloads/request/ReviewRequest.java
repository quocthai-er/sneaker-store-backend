package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReviewRequest {
    @NotBlank(message = "Content is required")
    private String content;
    @NotBlank(message = "Order item id is required")
    private String orderItemId;
    @NotNull(message = "Rate is required")
    @Range(min = 1, max = 5, message = "Invalid rate! Only from 1 to 5")
    private double rate;
}
