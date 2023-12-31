package com.example.sneakerstorebackend.controllers;

import com.example.sneakerstorebackend.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/manage/stats")
public class AdminController {
    private final AdminService adminService;

    @GetMapping(path = "/state")
    public ResponseEntity<?> getCountByState (){
        return adminService.getAllCountByState();
    }

    @GetMapping(path = "/orders")
    public ResponseEntity<?> getStats (@RequestParam(value = "from", defaultValue = "") String from,
                                       @RequestParam(value = "to", defaultValue = "") String to,
                                       @RequestParam("type") String type){
        return adminService.getOrderStatistical(from, to, type);
    }
}
