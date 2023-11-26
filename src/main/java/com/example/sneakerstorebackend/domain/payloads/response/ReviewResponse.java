package com.example.sneakerstorebackend.domain.payloads.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private String id;
    private String content;
    private double rate;
    private boolean enable;
    private String reviewedBy;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdDate;
}
