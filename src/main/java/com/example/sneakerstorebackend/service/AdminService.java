package com.example.sneakerstorebackend.service;

import org.springframework.http.ResponseEntity;

public interface AdminService {
    ResponseEntity<?> getOrderStatistical(String from, String to, String type);
    ResponseEntity<?> getAllCountByState();
}
