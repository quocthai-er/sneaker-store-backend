package com.example.sneakerstorebackend.mapper;

import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.response.CartItemResponse;
import com.example.sneakerstorebackend.entity.order.OrderItem;
import com.example.sneakerstorebackend.entity.product.ProductImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class CartMapper {

    public static CartItemResponse toCartItemRes(OrderItem orderItem) {
        Optional<ProductImage> image = Optional.ofNullable(orderItem.getItem().getProduct().getImages().stream().filter(x -> x.isThumbnail() && x.getColor().equals(orderItem.getColor())).findFirst()
                .orElse(orderItem.getItem().getProduct().getImages().get(0)));
        BigDecimal price = orderItem.getPrice();
        if (price.equals(BigDecimal.ZERO))
            price = orderItem.getItem().getProduct().getPrice().add(orderItem.getItem().getExtraFee());
        try {
            return new CartItemResponse(orderItem.getId(), orderItem.getItem().getProduct().getName(),
                    orderItem.getItem().getProduct().getDiscount(),
                    image.get().getUrl(), price,
                    orderItem.getItem().getId(), orderItem.getColor(), orderItem.getItem().getName(),
                    orderItem.getQuantity(), orderItem.getItem().getVariants().get(0).getStock(), orderItem.getSubPrice());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "get cart item failed");
        }
    }
}
