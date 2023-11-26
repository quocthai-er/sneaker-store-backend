package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.payloads.request.ReviewRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface ReviewService {
    ResponseEntity<?> addReview(String userId , ReviewRequest req);

    ResponseEntity<?> findByProductId(String productId , Pageable pageable);

}
