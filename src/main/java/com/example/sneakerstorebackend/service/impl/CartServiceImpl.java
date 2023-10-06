package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.CartRequest;
import com.example.sneakerstorebackend.domain.payloads.response.CartItemResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.entity.order.OrderItem;
import com.example.sneakerstorebackend.entity.product.ProductOption;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.mapper.CartMapper;
import com.example.sneakerstorebackend.repository.OrderItemRepository;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.repository.ProductOptionRepository;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.service.CartService;
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
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductOptionRepository productOptionRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addAndUpdateProductToCart(String userId, CartRequest req) {
        Optional<User> user = userRepository.findUserByIdAndState(userId, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isPresent()) {
            Optional<Order> order = orderRepository.findOrderByUser_IdAndState(new ObjectId(userId), ConstantsConfig.ORDER_STATE_ENABLE);
            if (order.isPresent()) {
                //Check if order already has product option with color
                Optional<OrderItem> item = order.get().getItems().stream().filter(
                        p -> p.getItem().getId().equals(req.getProductOptionId())
                                && p.getColor().equals(req.getColor())).findFirst();
                if (item.isPresent()) return processUpdateProductInCart(item.get(), req);
                else return processAddProductToExistOrder(order.get(), req, userId);
            } else return processAddProductToOrder(user.get(), req, userId);
        } throw new NotFoundException("Can not found user with id: "+userId);
    }

    @Transactional
    @Synchronized
    ResponseEntity<?> processAddProductToOrder(User user, CartRequest req, String userId) {
        if (req.getQuantity() <= 0) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid quantity");
        Optional<ProductOption> productOption = productOptionRepository.findByIdAndVariantColor(req.getProductOptionId(), req.getColor());
        if (productOption.isPresent()) {
            checkProductQuantity(productOption.get(), req);
            Order order = new Order(user, ConstantsConfig.ORDER_STATE_ENABLE);
            orderRepository.insert(order);
            OrderItem item = new OrderItem(productOption.get(), req.getColor(), req.getQuantity(), order);
            orderItemRepository.insert(item);
            CartItemResponse res = CartMapper.toCartItemRes(item);
            /*addScoreToRecommendation(productOption.get().getProduct().getCategory().getId(),
                    productOption.get().getProduct().getBrand().getId(), userId);*/
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(true, "Add product to cart first time success", res));
        } else throw new NotFoundException("Can not found product option with id: "+req.getProductOptionId());
    }

    private ResponseEntity<?> processAddProductToExistOrder(Order order, CartRequest req, String userId) {
        if (req.getQuantity() <= 0) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid quantity");
        Optional<ProductOption> productOption = productOptionRepository.findByIdAndVariantColor(req.getProductOptionId(), req.getColor());
        if (productOption.isPresent()) {
            checkProductQuantity(productOption.get(), req);
            OrderItem item = new OrderItem(productOption.get(), req.getColor(), req.getQuantity(), order);
            orderItemRepository.insert(item);
            CartItemResponse res = CartMapper.toCartItemRes(item);
            /*addScoreToRecommendation(productOption.get().getProduct().getCategory().getId(),
                    productOption.get().getProduct().getBrand().getId(), userId);*/
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(true, "Add product to cart success", res));
        } else throw new NotFoundException("Can not found product option with id: "+req.getProductOptionId());
    }

    private void checkProductQuantity(ProductOption productOption, CartRequest req) {
        productOption.getVariants().forEach(v -> {
            if (v.getColor().equals(req.getColor()) && v.getStock() < req.getQuantity()) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Quantity exceeds stock on product: "+req.getProductOptionId());
            }
        });
    }

    private ResponseEntity<?> processUpdateProductInCart(OrderItem orderItem, CartRequest req) {
        if (orderItem.getQuantity() + req.getQuantity() == 0) {
            orderItemRepository.deleteById(orderItem.getId());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Delete item "+orderItem.getId()+" in cart success", ""));
        }
        orderItem.getItem().getVariants().forEach(v -> {
            if (v.getColor().equals(req.getColor())) {
                long quantity = orderItem.getQuantity() + req.getQuantity();
                if (v.getStock() >= quantity && quantity > 0) {
                    orderItem.setQuantity(quantity);
                    orderItemRepository.save(orderItem);
                } else throw new AppException(HttpStatus.CONFLICT.value(), "Quantity invalid or exceeds stock on product: "+req.getProductOptionId());
            }
        });
        CartItemResponse res = CartMapper.toCartItemRes(orderItem);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(true, "Update product "+req.getProductOptionId()+" in cart success", res));
    }
}
