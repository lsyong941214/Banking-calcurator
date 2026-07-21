package com.example.engine.dto;

import java.math.BigDecimal;
import java.util.List;

public record InterestCalculationResponse(
        int termMonths,
        BigDecimal appliedRate,
        List<RepaymentDueAmount> results
) {
}
