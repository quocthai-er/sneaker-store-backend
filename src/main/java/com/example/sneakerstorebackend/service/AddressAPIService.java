package com.example.sneakerstorebackend.service;

import com.example.sneakerstorebackend.domain.exception.AppException;
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
public class AddressAPIService {
    @Value("${app.ghn.token}")
    private String TOKEN;
    @Value("${app.ghn.shop}")
    private String SHOP_ID;

    public ResponseEntity<?> getProvince(){
        try {
            HttpResponse<?> res = HttpConnectTemplate.connectToGHNAddress("master-data/province","", TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get province");
        }
    }

    public ResponseEntity<?> getDistrict(Long provinceId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("province_id", provinceId);
            HttpResponse<?> res = HttpConnectTemplate.connectToGHNAddress("master-data/district",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get district");
        }
    }

    public ResponseEntity<?> getWard(Long districtId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("district_id", districtId);
            HttpResponse<?> res = HttpConnectTemplate.connectToGHNAddress("master-data/ward?district_id",
                    body.toString(), TOKEN, SHOP_ID);
            return ResponseEntity.status(res.statusCode()).body(res.body());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when get ward");
        }
    }
}
