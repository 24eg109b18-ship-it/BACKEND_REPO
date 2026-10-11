package com.campus.lostfound.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of("service", "Campus Lost and Found API", "status", "running");
    }
}
