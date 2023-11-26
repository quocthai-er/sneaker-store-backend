package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ReviewRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Review;
import com.example.sneakerstorebackend.entity.order.OrderItem;
import com.example.sneakerstorebackend.entity.product.Product;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.repository.OrderItemRepository;
import com.example.sneakerstorebackend.repository.ProductRepository;
import com.example.sneakerstorebackend.repository.ReviewRepository;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.service.ReviewService;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    //private final ReviewMapper reviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Synchronized
    public ResponseEntity<?> addReview(String userId, ReviewRequest req) {
        Optional<Review> review = reviewRepository.findReviewByOrderItem_IdAndUser_Id(
                new ObjectId(req.getOrderItemId()), new ObjectId(userId));
        if (review.isPresent()) throw new AppException(HttpStatus.CONFLICT.value(), "You already review this product");
        Optional<User> user = userRepository.findUserByIdAndState(userId, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            Optional<OrderItem> orderItem = orderItemRepository.findById(req.getOrderItemId());
            if (orderItem.isPresent() && !orderItem.get().isReviewed()) {
                if (!orderItem.get().getOrder().getState().equals(ConstantsConfig.ORDER_STATE_DONE)
                        || !orderItem.get().getOrder().getUser().getId().equals(userId))
                    throw new AppException(HttpStatus.CONFLICT.value(), "You don't have permission");
                Optional<Product> product = productRepository.findProductByIdAndState(orderItem.get().getItem().getProduct().getId(), ConstantsConfig.ENABLE);
                if (product.isEmpty()) throw new NotFoundException("Can not found this product");
                Review newReview = new Review(req.getContent(), req.getRate(),
                        product.get(), orderItem.get(), user.get(), true);
                reviewRepository.save(newReview);
                double rate = ((product.get().getRate() * (product.get().getRateCount() - 1)) + req.getRate())/ product.get().getRateCount();
                product.get().setRate(rate);
                productRepository.save(product.get());
                orderItem.get().setReviewed(true);
                orderItemRepository.save(orderItem.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Add review success ", newReview));
            } throw new NotFoundException("Can not found order item or order item already reviewed with id: " + req.getOrderItemId());
        } throw new NotFoundException("Can not found user with id: " + userId);    }
}
