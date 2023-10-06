package com.example.sneakerstorebackend.entity.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductImage {
    //    @Id
    private String imageId;
    private String url;
    private boolean thumbnail;
   /* private String color;*/

}