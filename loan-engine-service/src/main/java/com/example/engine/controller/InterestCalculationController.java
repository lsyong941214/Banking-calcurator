package com.example.engine.controller;

import com.example.engine.dto.InterestCalculationRequest;
import com.example.engine.dto.InterestCalculationResponse;
import com.example.engine.service.InterestCalculationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
