package com.example.schedule.dto;

import com.example.schedule.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrentInstallmentResponse(
        PaymentStatus status,
        String statusLabel,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal appliedRate,
        BigDecimal overdueRate,
        BigDecimal principal,
        BigDecimal interest,
        long overdueDays,
        BigDecimal overduePrincipalInterest,
        BigDecimal overdueInterest,
        LocalDate accelerationStartDate,
        long accelerationDays,
        BigDecimal acceleratedPrincipalInterest,
        BigDecimal totalDueAmount
) {
}
