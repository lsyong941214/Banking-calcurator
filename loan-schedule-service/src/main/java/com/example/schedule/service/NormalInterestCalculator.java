package com.example.schedule.service;

import com.example.schedule.domain.RepaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 정상 이자계산: 다음이자납입일자를 기준으로 남은 회차 수(n)를 다시 산출해 그 회차의
 * 원금/이자를 계산한다 -- "1회차 = 최종이자납입일자~다음이자납입일자" 구간에 대한 이자를
 * 구하는 것이므로, 다음이자납입일자로부터 만기일자까지의 개월 수(+1, 지금 회차 포함)를
 * n으로 쓴다.
 */
@Component
class NormalInterestCalculator {

    private static final int MONEY_SCALE = 0;
    private static final int RATE_SCALE = 10;

    NormalInstallmentAmount calculate(
            RepaymentType repaymentType,
            BigDecimal balance,
            BigDecimal appliedRate,
            LocalDate nextIntPayDt,
            LocalDate maturityDate
    ) {
        BigDecimal monthlyRate = appliedRate.divide(BigDecimal.valueOf(12), RATE_SCALE, RoundingMode.HALF_UP);
        int remainingInstallments = Math.max(1, (int) ChronoUnit.MONTHS.between(nextIntPayDt, maturityDate) + 1);
        boolean isLastInstallment = remainingInstallments <= 1;

        BigDecimal interest = balance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal principal = calculatePrincipal(repaymentType, balance, monthlyRate, remainingInstallments, isLastInstallment, interest);

        return new NormalInstallmentAmount(principal, interest, remainingInstallments, isLastInstallment);
    }

    private BigDecimal calculatePrincipal(
            RepaymentType type,
            BigDecimal balance,
            BigDecimal monthlyRate,
            int remainingInstallments,
            boolean isLastInstallment,
            BigDecimal interest
    ) {
        if (isLastInstallment) {
            return balance;
        }

        return switch (type) {
            case EQUAL_PRINCIPAL_AND_INTEREST -> {
                BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(remainingInstallments);
                BigDecimal pmt = balance.multiply(monthlyRate).multiply(onePlusRToN)
                        .divide(onePlusRToN.subtract(BigDecimal.ONE), MONEY_SCALE, RoundingMode.HALF_UP);
                yield pmt.subtract(interest);
            }
            case EQUAL_PRINCIPAL -> balance.divide(BigDecimal.valueOf(remainingInstallments), MONEY_SCALE, RoundingMode.HALF_UP);
            case BULK -> BigDecimal.ZERO;
        };
    }

    record NormalInstallmentAmount(BigDecimal principal, BigDecimal interest, int remainingInstallments, boolean isLastInstallment) {
    }
}
