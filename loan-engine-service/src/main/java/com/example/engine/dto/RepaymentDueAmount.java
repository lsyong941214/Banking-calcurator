package com.example.engine.dto;

import com.example.engine.domain.RepaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentDueAmount(
        RepaymentType repaymentType,
        String repaymentTypeLabel,
        LocalDate dueDate,
        int remainingInstallments,
        BigDecimal principal,
        BigDecimal overduePrincipalInterest,
        BigDecimal interest,
        BigDecimal overdueInterest,
        long overdueDays,
        BigDecimal prepaymentInterest,
        long prepaymentDays,
        BigDecimal earlyRepaymentFee,
        BigDecimal totalDueAmount
) {
}
