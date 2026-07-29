package com.example.schedule.dto;

import java.math.BigDecimal;
import java.util.List;

public record RepaymentScheduleResponse(
        int termMonths,
        BigDecimal appliedRate,
        List<RepaymentScheduleTypeResult> results
) {
}
