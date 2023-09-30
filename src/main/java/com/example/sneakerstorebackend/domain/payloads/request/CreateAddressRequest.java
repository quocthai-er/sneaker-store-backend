package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class CreateAddressRequest {
    @NotBlank(message = "Province is required!")
    private String to_province_name;
    @NotBlank(message = "District is required!")
    private String to_district_name;
    @NotBlank(message = "Ward is required!")
    private String to_ward_name;
}
