package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class VerifyOTPRequest {
    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "Email is required")
    @Size( min = 5, max = 50)
    @Email(message = "Email invalidate")
    private String email;

    @NotBlank(message = "Type is required")
    private String type;
}
