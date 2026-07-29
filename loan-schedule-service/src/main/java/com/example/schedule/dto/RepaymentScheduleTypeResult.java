package com.example.schedule.dto;

import com.example.schedule.domain.RepaymentType;

import java.util.List;

public record RepaymentScheduleTypeResult(
        RepaymentType repaymentType,
        String repaymentTypeLabel,
        List<ScheduleInstallment> installments
) {
}
