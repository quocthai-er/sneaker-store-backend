package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank
    @Email(message = "Email invalidate")
    private String username;
    @NotBlank
    private String password;

    //private String otp;

}
