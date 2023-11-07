package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.OrderService;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;
    private final JwtUtils jwtUtils;

    @GetMapping(path = "/orders/{orderId}")
    public ResponseEntity<?> userFindOrderById (@PathVariable String orderId,
                                                HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        if (!user.getId().isBlank())
            return orderService.findOrderById(orderId, user.getId());
        throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission! Token is invalid");
    }

    @PutMapping(path = "/orders/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder (@PathVariable String orderId,
                                          HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        return orderService.cancelOrder(orderId, user.getId());
    }

    @GetMapping(path = "/manage/orders")
    public ResponseEntity<?> findAll (@RequestParam(defaultValue = "") String state,
                                      @PageableDefault(size = 5, sort = "lastModifiedDate") @ParameterObject Pageable pageable){
        return orderService.findAll(state, pageable);
    }

    @GetMapping(path = "/manage/orders/{orderId}")
    public ResponseEntity<?> findOrderById (@PathVariable String orderId) {
        return orderService.findOrderById(orderId);
    }

    @PutMapping(path = "/manage/orders/{state}/{orderId}")
    public ResponseEntity<?> changeState (@PathVariable String state,
                                          @PathVariable String orderId){
        return orderService.changeState(state, orderId);
    }

    @PutMapping(path = "/orders/done/{orderId}")
    public ResponseEntity<?> finishOrder (@PathVariable String orderId,
                                          HttpServletRequest request){
        User user = jwtUtils.getUserFromJWT(jwtUtils.getJwtFromHeader(request));
        return orderService.changeState(ConstantsConfig.ORDER_STATE_DONE, orderId, user.getId());
    }
}
