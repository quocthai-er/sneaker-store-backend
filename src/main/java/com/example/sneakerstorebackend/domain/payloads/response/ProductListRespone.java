package com.example.sneakerstorebackend.domain.payloads.response;

import com.example.sneakerstorebackend.entity.product.ProductAttribute;
import com.example.sneakerstorebackend.entity.product.ProductImage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListRespone {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private int discount;
/*    private double rate;
    private int rateCount;*/
    private String category;
    private String categoryName;
    private String brand;
    private String brandName;
    private String state;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime createdDate;
    private List<ProductAttribute> attr;
    private List<ProductImage> images;
}
