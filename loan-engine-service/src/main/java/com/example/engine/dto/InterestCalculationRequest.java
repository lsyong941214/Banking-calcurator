package com.example.engine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * baseRate/spreadRate/earlyRepaymentFeeRate are fractions (e.g. 0.0350 = 3.5%), not percentages.
 */
public record InterestCalculationRequest(
        LocalDate newDate,
        LocalDate maturityDate,
        BigDecimal baseRate,
        BigDecimal spreadRate,
        BigDecimal outstandingPrincipal,
        LocalDate referenceDate,
        BigDecimal earlyRepaymentFeeRate
) {
}
