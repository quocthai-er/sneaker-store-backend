package com.example.sneakerstorebackend.domain.exception;

import lombok.Data;

@Data
public class ExceptionResponse {
    private int status;
    private Object message;
    private boolean success;

    public ExceptionResponse(int status, Object message) {
        this.status = status;
        this.message = message;
        this.success = false;
    }
}
