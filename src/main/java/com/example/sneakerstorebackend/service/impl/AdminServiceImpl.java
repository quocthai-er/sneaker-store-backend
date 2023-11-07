package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.StateCountAggregate;
import com.example.sneakerstorebackend.domain.payloads.response.OrdersSaleResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.repository.*;
import com.example.sneakerstorebackend.service.AdminService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public ResponseEntity<?> getAllCountByState() {
        Map<String, Object> resp = new HashMap<>();
        try {
            Map<String, Object> order = processResponse(orderRepository.countAllByState());
            resp.put("order", order);
            Map<String, Object> product = processResponse(productRepository.countAllByState());
            resp.put("product", product);
            Map<String, Object> user = processResponse(userRepository.countAllByState());
            resp.put("user", user);
            Map<String, Object> category = processResponse(categoryRepository.countAllByState());
            resp.put("category", category);
            Map<String, Object> brand = processResponse(brandRepository.countAllByState());
            resp.put("brand", brand);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get count by state success", resp));
        } catch (Exception e) {
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
        }
    }

    private Map<String, Object> processResponse(List<StateCountAggregate> stateCountAggregates) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", stateCountAggregates);
        result.put("totalQuantity", stateCountAggregates.stream().map(StateCountAggregate::getCount).reduce(0L, Long::sum));
        return result;
    }

    @Override
    public ResponseEntity<?> getOrderStatistical(String from, String to, String type) {
        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = LocalDateTime.now();
        String pattern = "dd-MM-yyyy";
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        try {
            if (!from.isBlank()) fromDate = LocalDate.parse(from, df).atStartOfDay();
            if (!to.isBlank()) toDate = LocalDate.parse(to, df).atStartOfDay();
        } catch (DateTimeParseException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Incorrect date format");
        }
        Page<Order> orderList = orderRepository.findAllByCreatedDateBetweenAndState(fromDate, toDate, ConstantsConfig.ORDER_STATE_DONE, Pageable.unpaged());
        switch (type) {
            case "all":
                orderList = orderRepository.findAllByState(ConstantsConfig.ORDER_STATE_DONE, PageRequest.of(0, Integer.MAX_VALUE, Sort.by("lastModifiedDate").ascending()));
                pattern = "";
                break;
            case "month":
                pattern = "MM-yyyy";
                break;
            case "year":
                pattern = "yyyy";
                break;
        }
        List<OrdersSaleResponse> ordersSaleResList = getSaleAmount(orderList, pattern);
        return ordersSaleResList.size() > 0 ? ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(true, "Get orders sale successful", ordersSaleResList)) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject(false, "Can not found any order", "")
                );
    }

    public List<OrdersSaleResponse> getSaleAmount(Page<Order> orderList, String pattern) {
        List<OrdersSaleResponse> ordersSaleResList = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        if (orderList.getSize() > 0) {
            OrdersSaleResponse ordersSaleResponse = new OrdersSaleResponse();
            int quantity = 1;
            for (int i = 0; i <= orderList.getSize() - 1; i++) {
                String dateFormat = df.format(orderList.getContent().get(i).getLastModifiedDate());
                if (i == 0 || !ordersSaleResponse.getDate().equals(dateFormat)) {
                    if (i > 0) ordersSaleResList.add(ordersSaleResponse);
                    if (dateFormat.isBlank()) dateFormat = "all";
                    ordersSaleResponse = new OrdersSaleResponse(dateFormat,
                            orderList.getContent().get(i).getTotalPrice(), quantity);
                } else {
                    quantity++;
                    ordersSaleResponse.setAmount(ordersSaleResponse.getAmount().add(orderList.getContent().get(i).getTotalPrice()));
                    ordersSaleResponse.setQuantity(quantity);
                }
                if (i == orderList.getSize() - 1) ordersSaleResList.add(ordersSaleResponse);
            }
        }
        return ordersSaleResList;
    }
}
