package com.example.schedule.dto;

import com.example.schedule.domain.RepaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * baseRate/spreadRate는 소수(예: 0.0350 = 3.5%). deadlineLossDt는 미해당 계좌면 null.
 */
public record CurrentInstallmentRequest(
        RepaymentType repaymentType,
        LocalDate lastIntPayDt,
        LocalDate nextIntPayDt,
        LocalDate maturityDate,
        LocalDate deadlineLossDt,
        LocalDate referenceDate,
        BigDecimal baseRate,
        BigDecimal spreadRate,
        BigDecimal outstandingPrincipal
) {
}
