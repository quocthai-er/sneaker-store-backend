package com.example.sneakerstorebackend.domain.payloads.response;

import com.example.sneakerstorebackend.entity.user.EGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String id;
    private String email;
    private String name;
    private EGender gender;
    private String role;
    private String accessToken;
}
