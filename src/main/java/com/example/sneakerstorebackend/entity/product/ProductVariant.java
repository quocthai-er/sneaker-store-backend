package com.example.sneakerstorebackend.entity.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProductVariant {
    private UUID id;
/*    @NotBlank(message = "Color is required")
    private String color;*/
    @NotBlank(message = "Stock is required")
    private Long stock;
}
