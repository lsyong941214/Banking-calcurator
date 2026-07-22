package com.example.engine.dto;

import com.example.engine.domain.EarlyRepaymentFeeExemption;

import java.math.BigDecimal;
import java.util.List;

public record InterestCalculationResponse(
        int termMonths,
        BigDecimal appliedRate,
        BigDecimal earlyRepaymentFeeRateApplied,
        EarlyRepaymentFeeExemption earlyRepaymentFeeExemptionReason,
        boolean withdrawalPeriodApplicable,
        List<RepaymentDueAmount> results
) {
}
