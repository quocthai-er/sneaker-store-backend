package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class ChangeResetPasswordRequest {
    @NotBlank(message = "Temporary Password is required")
    @Size( min = 5, max = 50)
    private String tempPassword;
    @NotBlank(message = "New Password is required")
    @Size( min = 5, max = 50)
    private String newPassword;
}
