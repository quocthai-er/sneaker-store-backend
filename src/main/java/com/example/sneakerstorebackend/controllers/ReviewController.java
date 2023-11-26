package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.ReviewConstant;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.ReviewRequest;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(ReviewConstant.API_REVIEW)
public class ReviewController {

    private final JwtUtils jwtUtils;
    private final ReviewService reviewService;

    @PostMapping(ReviewConstant.API_ADD_REVIEW)
    public ResponseEntity<?> addReview (@Valid @RequestBody ReviewRequest req,
                                        HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return reviewService.addReview(user.getId(), req);
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @GetMapping(ReviewConstant.API_LIST_REVIEW)
    public ResponseEntity<?> findByProductId (@PathVariable("productId") String productId,
                                              @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable){
        return reviewService.findByProductId(productId, pageable);
    }
}
