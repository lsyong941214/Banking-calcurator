package com.example.schedule.service;

import com.example.schedule.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 연체 이자계산: PaymentStatusAnalyzer가 판정한 상태에 따라 분기한다.
 * - NORMAL: 연체 금액 없음.
 * - OVERDUE_INSTALLMENT: 다음이자납입일자+1영업일부터 조회시점까지, 해당 회차의 원금과
 *   이자 각각에 연체이율을 적용한다.
 * - ACCELERATED: 기한이익상실일자까지는 위와 동일하게 회차 단위로 연체이율을 적용하고,
 *   기한이익상실일자를 넘어선 구간은 회차 개념을 버리고 잔여원금 전체에 연체이율을 적용한다.
 */
@Component
class OverdueInterestCalculator {

    private static final int MONEY_SCALE = 0;

    OverdueAmount calculate(
            PaymentStatus status,
            BigDecimal installmentPrincipal,
            BigDecimal installmentInterest,
            BigDecimal outstandingBalance,
            BigDecimal overdueRate,
            int daysInYear,
            LocalDate overdueStartDate,
            LocalDate deadlineLossDt,
            LocalDate referenceDate
    ) {
        if (status == PaymentStatus.NORMAL) {
            return OverdueAmount.none();
        }

        LocalDate installmentOverdueEnd = status == PaymentStatus.ACCELERATED ? deadlineLossDt : referenceDate;
        long overdueDays = ChronoUnit.DAYS.between(overdueStartDate, installmentOverdueEnd);
        BigDecimal overduePrincipalInterest = proRate(installmentPrincipal, overdueRate, overdueDays, daysInYear);
        BigDecimal overdueInterest = proRate(installmentInterest, overdueRate, overdueDays, daysInYear);

        if (status != PaymentStatus.ACCELERATED) {
            return new OverdueAmount(overdueDays, overduePrincipalInterest, overdueInterest, null, 0, BigDecimal.ZERO);
        }

        long accelerationDays = ChronoUnit.DAYS.between(deadlineLossDt, referenceDate);
        BigDecimal acceleratedPrincipalInterest = proRate(outstandingBalance, overdueRate, accelerationDays, daysInYear);
        return new OverdueAmount(overdueDays, overduePrincipalInterest, overdueInterest, deadlineLossDt, accelerationDays, acceleratedPrincipalInterest);
    }

    private BigDecimal proRate(BigDecimal amount, BigDecimal rate, long days, int daysInYear) {
        if (days <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(rate).multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(daysInYear), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    record OverdueAmount(
            long overdueDays,
            BigDecimal overduePrincipalInterest,
            BigDecimal overdueInterest,
            LocalDate accelerationStartDate,
            long accelerationDays,
            BigDecimal acceleratedPrincipalInterest
    ) {
        static OverdueAmount none() {
            return new OverdueAmount(0, BigDecimal.ZERO, BigDecimal.ZERO, null, 0, BigDecimal.ZERO);
        }
    }
}
