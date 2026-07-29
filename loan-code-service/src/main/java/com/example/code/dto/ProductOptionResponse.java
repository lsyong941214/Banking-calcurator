package com.example.code.dto;

import java.util.List;

public record ProductOptionResponse(
        String code,
        String name,
        List<String> allowedRepaymentTypes,
        List<Integer> allowedLoanTermYears
) {
}
