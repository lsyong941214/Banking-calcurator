package com.example.engine.dto;

import com.example.engine.domain.RepaymentType;

import java.util.List;

public record RepaymentScheduleTypeResult(
        RepaymentType repaymentType,
        String repaymentTypeLabel,
        List<ScheduleInstallment> installments
) {
}
