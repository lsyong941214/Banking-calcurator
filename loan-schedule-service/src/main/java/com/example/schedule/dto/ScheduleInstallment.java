package com.example.schedule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleInstallment(
        int sequence,
        LocalDate dueDate,
        BigDecimal principalPayment,
        BigDecimal interestPayment,
        BigDecimal totalPayment,
        BigDecimal remainingBalance
) {
}
