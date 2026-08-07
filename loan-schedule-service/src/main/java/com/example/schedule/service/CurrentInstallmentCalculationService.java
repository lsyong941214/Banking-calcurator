package com.example.schedule.service;

import com.example.schedule.dto.CurrentInstallmentRequest;
import com.example.schedule.dto.CurrentInstallmentResponse;
import com.example.schedule.policy.LoanPolicyProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 회차 계산의 진입점. 정상 이자계산(NormalInterestCalculator)은 상태와 무관하게 항상
 * 실행하고(1회차분 원금/이자는 연체 여부와 관계없이 항상 발생하는 금액이므로), 연체
 * 이자계산(OverdueInterestCalculator)은 PaymentStatusAnalyzer가 판정한 상태에 따라
 * 분기해서 추가로 얹는다.
 */
@Service
public class CurrentInstallmentCalculationService {

    private final PaymentStatusAnalyzer statusAnalyzer;
    private final NormalInterestCalculator normalCalculator;
    private final OverdueInterestCalculator overdueCalculator;
    private final LoanPolicyProperties policy;

    public CurrentInstallmentCalculationService(
            PaymentStatusAnalyzer statusAnalyzer,
            NormalInterestCalculator normalCalculator,
            OverdueInterestCalculator overdueCalculator,
            LoanPolicyProperties policy
    ) {
        this.statusAnalyzer = statusAnalyzer;
        this.normalCalculator = normalCalculator;
        this.overdueCalculator = overdueCalculator;
        this.policy = policy;
    }

    public CurrentInstallmentResponse calculate(CurrentInstallmentRequest request) {
        validate(request);

        BigDecimal appliedRate = request.baseRate().add(request.spreadRate()).setScale(6, RoundingMode.HALF_UP);
        BigDecimal overdueRate = appliedRate.add(policy.getOverdueRateAddOn());

        NormalInterestCalculator.NormalInstallmentAmount base = normalCalculator.calculate(
                request.repaymentType(), request.outstandingPrincipal(), appliedRate,
                request.nextIntPayDt(), request.maturityDate()
        );

        PaymentStatusAnalyzer.PaymentStatusResult statusResult = statusAnalyzer.analyze(
                request.nextIntPayDt(), request.deadlineLossDt(), request.referenceDate()
        );

        OverdueInterestCalculator.OverdueAmount overdue = overdueCalculator.calculate(
                statusResult.status(), base.principal(), base.interest(), request.outstandingPrincipal(),
                overdueRate, policy.getDaysInYear(), statusResult.overdueStartDate(),
                request.deadlineLossDt(), request.referenceDate()
        );

        BigDecimal totalDueAmount = base.principal()
                .add(base.interest())
                .add(overdue.overduePrincipalInterest())
                .add(overdue.overdueInterest())
                .add(overdue.acceleratedPrincipalInterest());

        return new CurrentInstallmentResponse(
                statusResult.status(), statusResult.status().label(),
                request.lastIntPayDt(), request.nextIntPayDt(),
                appliedRate, overdueRate,
                base.principal(), base.interest(),
                overdue.overdueDays(), overdue.overduePrincipalInterest(), overdue.overdueInterest(),
                overdue.accelerationStartDate(), overdue.accelerationDays(), overdue.acceleratedPrincipalInterest(),
                totalDueAmount
        );
    }

    private void validate(CurrentInstallmentRequest request) {
        if (request.repaymentType() == null) {
            throw new IllegalArgumentException("상환방식은 필수입니다.");
        }
        if (request.lastIntPayDt() == null || request.nextIntPayDt() == null || request.maturityDate() == null || request.referenceDate() == null) {
            throw new IllegalArgumentException("최종이자납입일자, 다음이자납입일자, 만기일자, 계산기준일자는 필수입니다.");
        }
        if (!request.nextIntPayDt().isAfter(request.lastIntPayDt())) {
            throw new IllegalArgumentException("다음이자납입일자는 최종이자납입일자보다 이후여야 합니다.");
        }
        if (request.maturityDate().isBefore(request.nextIntPayDt())) {
            throw new IllegalArgumentException("만기일자는 다음이자납입일자보다 이후여야 합니다.");
        }
        if (request.baseRate() == null || request.spreadRate() == null) {
            throw new IllegalArgumentException("기준금리와 가산금리는 필수입니다.");
        }
        if (request.outstandingPrincipal() == null || request.outstandingPrincipal().signum() < 0) {
            throw new IllegalArgumentException("대출잔액은 0 이상이어야 합니다.");
        }
    }
}
