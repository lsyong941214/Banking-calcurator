package com.example.engine.controller;

import com.example.engine.dto.InterestCalculationRequest;
import com.example.engine.dto.InterestCalculationResponse;
import com.example.engine.service.InterestCalculationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InterestCalculationController {

    private final InterestCalculationService service;

    public InterestCalculationController(InterestCalculationService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/interest-calculations")
    public InterestCalculationResponse calculate(@RequestBody InterestCalculationRequest request) {
        return service.calculate(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
