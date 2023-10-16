package com.example.sneakerstorebackend.domain.payloads.response;

import com.example.sneakerstorebackend.entity.order.DeliveryDetail;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class OrderResponse {
    private String id;
    private String userId;
    private String userName;
    private long totalProduct = 0;
    private BigDecimal totalPrice;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CartItemResponse> items = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String paymentType;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private DeliveryDetail deliveryDetail;
    private String state;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Object createdDate;
    private Map<String, Object> paymentInfo;

    public OrderResponse(String id, String userId, String userName, long totalProduct, BigDecimal totalPrice, String state, Object createdDate) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.totalProduct = totalProduct;
        this.totalPrice = totalPrice;
        this.state = state;
        this.createdDate = createdDate;
    }
}
