package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressRequest {
    private Long province_id;
    private Long district_id;
}
