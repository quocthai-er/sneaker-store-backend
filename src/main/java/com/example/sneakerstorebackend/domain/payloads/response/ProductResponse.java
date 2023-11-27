package com.example.sneakerstorebackend.domain.payloads.response;

import com.example.sneakerstorebackend.entity.product.ProductAttribute;
import com.example.sneakerstorebackend.entity.product.ProductImage;
import com.example.sneakerstorebackend.entity.product.ProductOption;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private int discount;
    private double rate;
    private int rateCount;
    private String category;
    private String categoryName;
    private String brand;
    private String brandName;
    private String state;
    private List<ProductAttribute> attr;
    private List<ProductOption> options;
    private List<ProductImage> images;
}
