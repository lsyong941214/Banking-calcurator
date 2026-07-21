package com.example.engine.service;

import com.example.engine.domain.RepaymentType;
import com.example.engine.dto.InterestCalculationRequest;
import com.example.engine.dto.InterestCalculationResponse;
import com.example.engine.dto.RepaymentDueAmount;
import com.example.engine.policy.LoanPolicyProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Pure input-driven interest calculation simulation: no installment/payment history
 * is tracked (that will come once the account concept is introduced), so the "due
 * date" used for overdue/prepayment comparison is always the loan's first scheduled
 * payment (newDate + 1 month), and the repayment-method formulas always use the full
 * contract term as n. referenceDate is only used to measure days early/late against
 * that single reference due date.
 */
@Service
public class InterestCalculationService {

    private static final int MONEY_SCALE = 0;
    private static final int RATE_SCALE = 10;

    private final LoanPolicyProperties policy;

    public InterestCalculationService(LoanPolicyProperties policy) {
        this.policy = policy;
    }

    public InterestCalculationResponse calculate(InterestCalculationRequest request) {
        validate(request);

        LocalDate newDate = request.newDate();
        LocalDate maturityDate = request.maturityDate();
        LocalDate referenceDate = request.referenceDate();
        BigDecimal balance = request.outstandingPrincipal();

        int termMonths = (int) ChronoUnit.MONTHS.between(newDate, maturityDate);
        BigDecimal appliedRate = request.baseRate().add(request.spreadRate()).setScale(6, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = appliedRate.divide(BigDecimal.valueOf(12), RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal earlyRepaymentFeeRate = request.earlyRepaymentFeeRate() != null
                ? request.earlyRepaymentFeeRate()
                : policy.getEarlyRepaymentFeeRate();

        LocalDate dueDate = newDate.plusMonths(1);
        int remainingInstallments = termMonths;
        boolean isLastInstallment = termMonths <= 1;

        long daysDiff = ChronoUnit.DAYS.between(referenceDate, dueDate); // >0: 선납, <0: 연체
        long prepaymentDays = Math.max(daysDiff, 0);
        long overdueDays = Math.max(-daysDiff, 0);

        List<RepaymentDueAmount> results = List.of(
                buildResult(RepaymentType.EQUAL_PRINCIPAL_AND_INTEREST, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate, earlyRepaymentFeeRate),
                buildResult(RepaymentType.EQUAL_PRINCIPAL, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate, earlyRepaymentFeeRate),
                buildResult(RepaymentType.BULK, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate, earlyRepaymentFeeRate)
        );

        return new InterestCalculationResponse(termMonths, appliedRate, earlyRepaymentFeeRate, results);
    }

    private RepaymentDueAmount buildResult(
            RepaymentType type,
            BigDecimal balance,
            BigDecimal monthlyRate,
            BigDecimal appliedRate,
            int remainingInstallments,
            boolean isLastInstallment,
            LocalDate dueDate,
            long prepaymentDays,
            long overdueDays,
            LocalDate newDate,
            LocalDate maturityDate,
            LocalDate referenceDate,
            BigDecimal earlyRepaymentFeeRate
    ) {
        BigDecimal interest = balance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal principal = calculatePrincipal(type, balance, monthlyRate, remainingInstallments, isLastInstallment, interest);

        BigDecimal overdueRate = appliedRate.add(policy.getOverdueRateAddOn());
        BigDecimal daysInYear = BigDecimal.valueOf(policy.getDaysInYear());

        BigDecimal overduePrincipalInterest = overdueDays == 0
                ? BigDecimal.ZERO
                : principal.multiply(overdueRate).multiply(BigDecimal.valueOf(overdueDays))
                        .divide(daysInYear, MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal overdueInterest = overdueDays == 0
                ? BigDecimal.ZERO
                : interest.multiply(overdueRate).multiply(BigDecimal.valueOf(overdueDays))
                        .divide(daysInYear, MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal prepaymentInterest = prepaymentDays == 0
                ? BigDecimal.ZERO
                : balance.multiply(appliedRate).multiply(BigDecimal.valueOf(prepaymentDays))
                        .divide(daysInYear, MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal earlyRepaymentFee = calculateEarlyRepaymentFee(balance, newDate, maturityDate, referenceDate, earlyRepaymentFeeRate);

        BigDecimal totalDueAmount = principal
                .add(overduePrincipalInterest)
                .add(interest)
                .add(overdueInterest)
                .subtract(prepaymentInterest)
                .add(earlyRepaymentFee);

        return new RepaymentDueAmount(
                type, type.label(), dueDate, remainingInstallments,
                principal, overduePrincipalInterest,
                interest, overdueInterest, overdueDays,
                prepaymentInterest, prepaymentDays,
                earlyRepaymentFee, totalDueAmount
        );
    }

    private BigDecimal calculatePrincipal(
            RepaymentType type,
            BigDecimal balance,
            BigDecimal monthlyRate,
            int remainingInstallments,
            boolean isLastInstallment,
            BigDecimal interest
    ) {
        if (isLastInstallment || remainingInstallments <= 1) {
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

    private BigDecimal calculateEarlyRepaymentFee(
            BigDecimal balance,
            LocalDate newDate,
            LocalDate maturityDate,
            LocalDate referenceDate,
            BigDecimal earlyRepaymentFeeRate
    ) {
        if (!referenceDate.isBefore(maturityDate) || earlyRepaymentFeeRate.signum() == 0) {
            return BigDecimal.ZERO;
        }
        long remainingDays = ChronoUnit.DAYS.between(referenceDate, maturityDate);
        long totalLoanDays = ChronoUnit.DAYS.between(newDate, maturityDate);
        if (totalLoanDays <= 0) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(earlyRepaymentFeeRate)
                .multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalLoanDays), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private void validate(InterestCalculationRequest request) {
        if (request.newDate() == null || request.maturityDate() == null || request.referenceDate() == null) {
            throw new IllegalArgumentException("신규일자, 만기일자, 기준일자는 필수입니다.");
        }
        if (!request.maturityDate().isAfter(request.newDate())) {
            throw new IllegalArgumentException("만기일자는 신규일자보다 이후여야 합니다.");
        }
        if (request.baseRate() == null || request.spreadRate() == null) {
            throw new IllegalArgumentException("기준금리와 가산금리는 필수입니다.");
        }
        if (request.outstandingPrincipal() == null || request.outstandingPrincipal().signum() < 0) {
            throw new IllegalArgumentException("대출잔액은 0 이상이어야 합니다.");
        }
        if (request.earlyRepaymentFeeRate() != null && request.earlyRepaymentFeeRate().signum() < 0) {
            throw new IllegalArgumentException("중도상환수수료율은 0 이상이어야 합니다.");
        }
    }
}
