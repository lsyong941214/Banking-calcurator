package com.example.schedule.controller;

import com.example.schedule.dto.CurrentInstallmentRequest;
import com.example.schedule.dto.CurrentInstallmentResponse;
import com.example.schedule.service.CurrentInstallmentCalculationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentInstallmentController {

    private final CurrentInstallmentCalculationService service;

    public CurrentInstallmentController(CurrentInstallmentCalculationService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/repayment-schedules/current-installment")
    public CurrentInstallmentResponse calculate(@RequestBody CurrentInstallmentRequest request) {
        return service.calculate(request);
    }
}
