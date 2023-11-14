package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShippingItemRequest {
    private String name;
    private int quantity;
}
