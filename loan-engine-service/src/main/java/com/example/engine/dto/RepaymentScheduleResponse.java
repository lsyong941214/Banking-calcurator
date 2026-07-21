package com.example.engine.dto;

import java.math.BigDecimal;
import java.util.List;

public record RepaymentScheduleResponse(
        int termMonths,
        BigDecimal appliedRate,
        List<RepaymentScheduleTypeResult> results
) {
}
