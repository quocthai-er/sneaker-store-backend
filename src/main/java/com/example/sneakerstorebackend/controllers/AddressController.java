package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.domain.constant.AddressConstant;
import com.example.sneakerstorebackend.domain.payloads.request.AddressRequest;
import com.example.sneakerstorebackend.service.AddressAPIService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(AddressConstant.API_ADDRESS)
public class AddressController {
    private final AddressAPIService addressAPIService;

    @PostMapping(path = "/province", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProvince () {
        return addressAPIService.getProvince();
    }

    @PostMapping(path = "/district", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDistrict (@RequestBody AddressRequest req) {
        return addressAPIService.getDistrict(req.getProvince_id());
    }

    @PostMapping(path = "/ward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getWard (@RequestBody AddressRequest req) {
        return addressAPIService.getWard(req.getDistrict_id());
    }
}
