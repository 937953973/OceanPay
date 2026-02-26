package com.oceanpay.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WelcomeController {
    
    @GetMapping("/welcome")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to OceanPay API");
        response.put("version", "1.0.0");
        response.put("status", "active");
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "OceanPay Payment System");
        info.put("description", "A payment processing system");
        info.put("environment", "development");
        info.put("javaVersion", System.getProperty("java.version"));
        return info;
    }
}