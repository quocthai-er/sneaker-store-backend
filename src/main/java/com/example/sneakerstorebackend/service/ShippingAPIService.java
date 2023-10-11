package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.payloads.request.ShippingRequest;
import com.example.sneakerstorebackend.util.HttpConnectTemplate;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;

@Service
@Slf4j
public class ShippingAPIService {
    @Value("${app.ghn.token}")
    private String TOKEN;
    @Value("${app.ghn.shop}")
    private String SHOP_ID;

    public ResponseEntity<?> getDetail(String id) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("client_order_code", id);
            HttpResponse<?> res = HttpConnectTemplate.connectToGHN("v2/shipping-order/detail-by-client-code",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get detail");
        }
    }

    public ResponseEntity<?> calculateFee(ShippingRequest req) {
        if (req.getWeight() > 30000) req.setWeight(30000L);
        if (req.getHeight() > 150) req.setHeight(150L);
        try {
            JsonObject body = new JsonObject();
            body.addProperty("service_type_id", req.getService_type_id());
            body.addProperty("to_district_id", req.getTo_district_id());
            body.addProperty("to_ward_code", req.getTo_ward_code());
            body.addProperty("weight", req.getWeight());
            body.addProperty("length",100);
            body.addProperty("width",50);
            body.addProperty("height",req.getHeight());

            HttpResponse<?> res = HttpConnectTemplate.connectToGHN("v2/shipping-order/fee",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get fee");
        }
    }

    public ResponseEntity<?> calculateExpectedDeliveryTime(ShippingRequest req) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("service_id", req.getService_type_id());
            body.addProperty("to_district_id", req.getTo_district_id());
            body.addProperty("to_ward_code", req.getTo_ward_code());

            HttpResponse<?> res = HttpConnectTemplate.connectToGHN("v2/shipping-order/leadtime",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get expected delivery time");
        }
    }

    public ResponseEntity<?> getService(ShippingRequest req) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("shop_id", Integer.parseInt(SHOP_ID));
            body.addProperty("to_district", req.getTo_district_id());
            body.addProperty("from_district", 3695);

            HttpResponse<?> res = HttpConnectTemplate.connectToGHN("v2/shipping-order/available-services",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get service");
        }
    }
}
