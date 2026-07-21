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
 * Stateless calculation: given a loan's terms and a reference date, works out what
 * is due (or refundable / penalized) on that date for each repayment method.
 *
 * There is no persisted payment history input (no "which installment" or "last paid
 * date" field), so the "current installment" is chosen as whichever scheduled due
 * date is nearest to the reference date. If that due date has already passed, the
 * gap is treated as overdue (연체); if it hasn't arrived yet, the gap is treated as
 * an early-payment window (선납). This is a simplifying assumption for the first
 * version of the screen — revisit if the business wants explicit installment/
 * payment-history tracking instead.
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

        int selectedIndex = selectCurrentInstallmentIndex(newDate, referenceDate, termMonths);
        LocalDate dueDate = newDate.plusMonths(selectedIndex);
        int remainingInstallments = termMonths - selectedIndex + 1;
        boolean isLastInstallment = selectedIndex == termMonths;

        long daysDiff = ChronoUnit.DAYS.between(referenceDate, dueDate); // >0: 선납, <0: 연체
        long prepaymentDays = Math.max(daysDiff, 0);
        long overdueDays = Math.max(-daysDiff, 0);

        List<RepaymentDueAmount> results = List.of(
                buildResult(RepaymentType.EQUAL_PRINCIPAL_AND_INTEREST, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate),
                buildResult(RepaymentType.EQUAL_PRINCIPAL, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate),
                buildResult(RepaymentType.BULK, balance, monthlyRate, appliedRate,
                        remainingInstallments, isLastInstallment, dueDate, prepaymentDays, overdueDays,
                        newDate, maturityDate, referenceDate)
        );

        return new InterestCalculationResponse(termMonths, appliedRate, results);
    }

    private int selectCurrentInstallmentIndex(LocalDate newDate, LocalDate referenceDate, int termMonths) {
        long elapsedMonths = ChronoUnit.MONTHS.between(newDate, referenceDate);
        long priorIndex = Math.max(0, Math.min(elapsedMonths, termMonths));
        long nextIndex = Math.min(priorIndex + 1, termMonths);

        if (priorIndex == 0) {
            return (int) nextIndex;
        }

        LocalDate priorDueDate = newDate.plusMonths(priorIndex);
        LocalDate nextDueDate = newDate.plusMonths(nextIndex);

        long distToPrior = ChronoUnit.DAYS.between(priorDueDate, referenceDate);
        long distToNext = ChronoUnit.DAYS.between(referenceDate, nextDueDate);

        return (int) (distToPrior <= distToNext ? priorIndex : nextIndex);
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
            LocalDate referenceDate
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

        BigDecimal earlyRepaymentFee = calculateEarlyRepaymentFee(balance, newDate, maturityDate, referenceDate);

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

    private BigDecimal calculateEarlyRepaymentFee(BigDecimal balance, LocalDate newDate, LocalDate maturityDate, LocalDate referenceDate) {
        if (!referenceDate.isBefore(maturityDate)) {
            return BigDecimal.ZERO;
        }
        long remainingDays = ChronoUnit.DAYS.between(referenceDate, maturityDate);
        long totalLoanDays = ChronoUnit.DAYS.between(newDate, maturityDate);
        if (totalLoanDays <= 0) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(policy.getEarlyRepaymentFeeRate())
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
    }
}
