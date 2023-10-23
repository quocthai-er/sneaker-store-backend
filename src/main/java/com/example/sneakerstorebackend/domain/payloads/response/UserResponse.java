package com.example.sneakerstorebackend.domain.payloads.response;

import com.example.sneakerstorebackend.entity.user.EGender;
import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String phone;
    private Integer province;
    private Integer district;
    private Integer ward;
    private String address;
    private EGender gender;
    private String role;
    private String state;
}
