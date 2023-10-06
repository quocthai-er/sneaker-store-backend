package com.example.sneakerstorebackend.domain.payloads.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private String itemId;
    private String name;
    private int discount;
    private String image;
    private BigDecimal price;
    private String productOptionId;
   /* private String color;*/
    private String size;
    private long quantity;
    private long stock;
    private BigDecimal subPrice;
    //private boolean reviewed;
}
