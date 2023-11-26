package com.example.sneakerstorebackend.mapper;

import com.example.sneakerstorebackend.domain.payloads.response.ReviewResponse;
import com.example.sneakerstorebackend.entity.Review;
import org.springframework.stereotype.Service;

@Service
public class ReviewMapper {
    public ReviewResponse toReviewResponse(Review req) {
        return new ReviewResponse(req.getId(), req.getContent(), req.getRate(),
                req.isEnable(), req.getUser().getName(), req.getCreatedDate());
    }
}
