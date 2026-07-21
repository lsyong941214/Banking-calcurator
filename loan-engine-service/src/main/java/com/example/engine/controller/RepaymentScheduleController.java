package com.example.engine.controller;

import com.example.engine.dto.RepaymentScheduleRequest;
import com.example.engine.dto.RepaymentScheduleResponse;
import com.example.engine.service.RepaymentScheduleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepaymentScheduleController {

    private final RepaymentScheduleService service;

    public RepaymentScheduleController(RepaymentScheduleService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/repayment-schedules")
    public RepaymentScheduleResponse generate(@RequestBody RepaymentScheduleRequest request) {
        return service.generate(request);
    }
}
