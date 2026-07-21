package com.example.engine.service;

import com.example.engine.domain.RepaymentType;
import com.example.engine.dto.RepaymentScheduleRequest;
import com.example.engine.dto.RepaymentScheduleResponse;
import com.example.engine.dto.RepaymentScheduleTypeResult;
import com.example.engine.dto.ScheduleInstallment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Full amortization schedule generation for each repayment method, using BigDecimal
 * throughout and absorbing rounding drift into the final installment so the last
 * remainingBalance is always exactly zero.
 */
@Service
public class RepaymentScheduleService {

    private static final int MONEY_SCALE = 0;
    private static final int RATE_SCALE = 10;

    public RepaymentScheduleResponse generate(RepaymentScheduleRequest request) {
        validate(request);

        LocalDate newDate = request.newDate();
        LocalDate maturityDate = request.maturityDate();
        BigDecimal principal = request.principal();

        int termMonths = (int) ChronoUnit.MONTHS.between(newDate, maturityDate);
        BigDecimal appliedRate = request.baseRate().add(request.spreadRate()).setScale(6, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = appliedRate.divide(BigDecimal.valueOf(12), RATE_SCALE, RoundingMode.HALF_UP);

        List<RepaymentScheduleTypeResult> results = List.of(
                new RepaymentScheduleTypeResult(
                        RepaymentType.EQUAL_PRINCIPAL_AND_INTEREST,
                        RepaymentType.EQUAL_PRINCIPAL_AND_INTEREST.label(),
                        generateEqualPrincipalAndInterest(principal, monthlyRate, termMonths, newDate)
                ),
                new RepaymentScheduleTypeResult(
                        RepaymentType.EQUAL_PRINCIPAL,
                        RepaymentType.EQUAL_PRINCIPAL.label(),
                        generateEqualPrincipal(principal, monthlyRate, termMonths, newDate)
                ),
                new RepaymentScheduleTypeResult(
                        RepaymentType.BULK,
                        RepaymentType.BULK.label(),
                        generateBulk(principal, monthlyRate, termMonths, newDate)
                )
        );

        return new RepaymentScheduleResponse(termMonths, appliedRate, results);
    }

    private List<ScheduleInstallment> generateEqualPrincipalAndInterest(
            BigDecimal principal, BigDecimal monthlyRate, int months, LocalDate newDate
    ) {
        List<ScheduleInstallment> schedule = new ArrayList<>();

        BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(months);
        BigDecimal monthlyPayment = principal.multiply(monthlyRate).multiply(onePlusRToN)
                .divide(onePlusRToN.subtract(BigDecimal.ONE), MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            BigDecimal totalPayment = monthlyPayment;

            if (i == months) {
                principalPayment = remainingBalance;
                totalPayment = principalPayment.add(interestPayment);
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalPayment);
            }

            schedule.add(new ScheduleInstallment(i, newDate.plusMonths(i), principalPayment, interestPayment, totalPayment, remainingBalance));
        }

        return schedule;
    }

    private List<ScheduleInstallment> generateEqualPrincipal(
            BigDecimal principal, BigDecimal monthlyRate, int months, LocalDate newDate
    ) {
        List<ScheduleInstallment> schedule = new ArrayList<>();

        BigDecimal basePrincipalPayment = principal.divide(BigDecimal.valueOf(months), MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalPayment = basePrincipalPayment;

            if (i == months) {
                principalPayment = remainingBalance;
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalPayment);
            }

            BigDecimal totalPayment = principalPayment.add(interestPayment);
            schedule.add(new ScheduleInstallment(i, newDate.plusMonths(i), principalPayment, interestPayment, totalPayment, remainingBalance));
        }

        return schedule;
    }

    private List<ScheduleInstallment> generateBulk(
            BigDecimal principal, BigDecimal monthlyRate, int months, LocalDate newDate
    ) {
        List<ScheduleInstallment> schedule = new ArrayList<>();

        BigDecimal interestPayment = principal.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        for (int i = 1; i <= months; i++) {
            BigDecimal principalPayment = (i == months) ? principal : BigDecimal.ZERO;
            BigDecimal remainingBalance = (i == months) ? BigDecimal.ZERO : principal;
            BigDecimal totalPayment = principalPayment.add(interestPayment);

            schedule.add(new ScheduleInstallment(i, newDate.plusMonths(i), principalPayment, interestPayment, totalPayment, remainingBalance));
        }

        return schedule;
    }

    private void validate(RepaymentScheduleRequest request) {
        if (request.newDate() == null || request.maturityDate() == null) {
            throw new IllegalArgumentException("신규일자와 만기일자는 필수입니다.");
        }
        if (!request.maturityDate().isAfter(request.newDate())) {
            throw new IllegalArgumentException("만기일자는 신규일자보다 이후여야 합니다.");
        }
        if (request.baseRate() == null || request.spreadRate() == null) {
            throw new IllegalArgumentException("기준금리와 가산금리는 필수입니다.");
        }
        if (request.principal() == null || request.principal().signum() <= 0) {
            throw new IllegalArgumentException("대출잔액은 0보다 커야 합니다.");
        }
    }
}
