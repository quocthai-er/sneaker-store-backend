package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.CheckoutRequest;
import com.example.sneakerstorebackend.entity.PaymentDetail;
import com.example.sneakerstorebackend.entity.order.DeliveryDetail;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.entity.user.User;
import com.example.sneakerstorebackend.repository.OrderItemRepository;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.repository.UserRepository;
import com.example.sneakerstorebackend.security.jwt.JwtUtils;
import com.example.sneakerstorebackend.service.PaymentFactory;
import com.example.sneakerstorebackend.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ApplicationContext context;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final JwtUtils jwtTokenUtil;
    private final UserRepository userRepository;

    public static String CLIENT_REDIRECT = "http://localhost:3000/redirect/payment?success=";


    public PaymentFactory getPaymentMethod(String methodName) {
        switch (methodName) {
            case ConstantsConfig.PAYMENT_PAYPAL: return context.getBean(PaypalServiceImpl.class);
            case ConstantsConfig.PAYMENT_COD: return context.getBean(CODServiceImpl.class);
            default: return null;
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> createPayment(HttpServletRequest request, String id, String paymentType, CheckoutRequest req) {
        String user_id = jwtTokenUtil.getUserFromJWT(jwtTokenUtil.getJwtFromHeader(request)).getId();
        if (user_id.isBlank()) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Token is invalid");
        Optional<Order> order;
        try {
            order = orderRepository.findOrderByUser_IdAndState(new ObjectId(user_id), ConstantsConfig.ORDER_STATE_ENABLE);
            if (order.isEmpty() || !order.get().getId().equals(id)) {
                throw new NotFoundException("Can not found any order with id: " + id);
            }
            PaymentDetail paymentDetail = new PaymentDetail(null,paymentType.toUpperCase(), "", new HashMap<>());
            paymentDetail.getPaymentInfo().put("orderDate", LocalDateTime.now(Clock.systemDefaultZone()));
            order.get().setPaymentDetail(paymentDetail);
            DeliveryDetail deliveryDetail = new DeliveryDetail(req.getName(), req.getPhone(),
                    req.getProvince(), req.getDistrict(), req.getWard(),req.getAddress());
            order.get().setDeliveryDetail(deliveryDetail);
            order.get().getDeliveryDetail().getDeliveryInfo().put("fee", req.getShippingFee());
            order.get().getDeliveryDetail().getDeliveryInfo().put("serviceType", req.getServiceType());
            order.get().getDeliveryDetail().getDeliveryInfo().put("expectedDeliveryTime", req.getExpectedDeliveryTime());
            order.get().getDeliveryDetail().getDeliveryInfo().put("fullAddress", req.getFullAddress());
            order.get().setState(ConstantsConfig.ORDER_STATE_PROCESS);
            order.get().getItems().forEach(item -> item.setPrice(new BigDecimal((item.getItem().getProduct().getPrice()
                    .add(item.getItem().getExtraFee()))
                    .multiply(BigDecimal.valueOf((double) (100- item.getItem().getProduct().getDiscount())/100))
                    .stripTrailingZeros().toPlainString())));
            orderItemRepository.saveAll(order.get().getItems());
            orderRepository.save(order.get());
        } catch (NotFoundException e) {
            log.error(e.getMessage());
            throw new NotFoundException(e.getMessage());
        }catch (AppException e) {
            throw new AppException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "More than one cart with user id: "+ user_id);
        }
        PaymentFactory paymentFactory = getPaymentMethod(paymentType);
        return paymentFactory.createPayment(request, order.get());
    }

    @Override
    @Transactional
    public ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response) {
        if (paymentId != null && payerId != null ) {
            PaymentFactory paymentFactory = getPaymentMethod(ConstantsConfig.PAYMENT_PAYPAL);
            return paymentFactory.executePayment(paymentId, payerId, null,null, request, response);
        } else if (responseCode != null) {
            PaymentFactory paymentFactory = getPaymentMethod(ConstantsConfig.PAYMENT_VNPAY);
            return paymentFactory.executePayment(null, null, responseCode, id, request, response);
        } else {
            checkRoleForCODPayment(request);
            PaymentFactory paymentFactory = getPaymentMethod(ConstantsConfig.PAYMENT_COD);
            return paymentFactory.executePayment(paymentId, null, null,null, request, response);
        }
    }

    private void checkRoleForCODPayment(HttpServletRequest request) {
        String userId = jwtTokenUtil.getUserFromJWT(jwtTokenUtil.getJwtFromHeader(request)).getId();
        Optional<User> user = userRepository.findUserByIdAndState(userId, ConstantsConfig.USER_STATE_ACTIVATED);
        if (user.isEmpty() ||
                !(user.get().getRole().equals(ConstantsConfig.ROLE_ADMIN) || user.get().getRole().equals(ConstantsConfig.ROLE_STAFF)))
            throw new AppException(HttpStatus.FORBIDDEN.value(), "You don't have permission!");
    }
}
