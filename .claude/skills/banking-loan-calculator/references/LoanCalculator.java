import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference implementation for loan-engine-service.
 * All money/rate math uses BigDecimal + RoundingMode.HALF_UP — never double/float.
 * Every method absorbs rounding drift into the final installment so the last
 * remainingBalance is always exactly zero.
 */
public class LoanCalculator {

    public record ScheduleDto(
            int sequence,
            LocalDate dueDate,
            BigDecimal principalPayment,
            BigDecimal interestPayment,
            BigDecimal totalPayment,
            BigDecimal remainingBalance
    ) {}

    // 원리금균등상환 (Equal Principal & Interest)
    // PMT = P * r * (1+r)^n / ((1+r)^n - 1), r = monthly rate = annualRate / 12
    public static List<ScheduleDto> calculateEqualPrincipalAndInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            int months,
            LocalDate startDate
    ) {
        List<ScheduleDto> schedules = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(months);
        BigDecimal monthlyPayment = principal.multiply(monthlyRate).multiply(onePlusRToN)
                .divide(onePlusRToN.subtract(BigDecimal.ONE), 0, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);

            if (i == months) {
                // 마지막 회차 단수차 조정: 잔액을 그대로 원금으로 흡수
                principalPayment = remainingBalance;
                monthlyPayment = principalPayment.add(interestPayment);
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalPayment);
            }

            schedules.add(new ScheduleDto(
                    i, startDate.plusMonths(i),
                    principalPayment, interestPayment, monthlyPayment, remainingBalance
            ));
        }

        return schedules;
    }

    // 원금균등상환 (Equal Principal)
    // 매회 원금 = P / n (고정), 이자는 잔액 기준으로 매회 감소
    public static List<ScheduleDto> calculateEqualPrincipal(
            BigDecimal principal,
            BigDecimal annualRate,
            int months,
            LocalDate startDate
    ) {
        List<ScheduleDto> schedules = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal basePrincipalPayment = principal.divide(BigDecimal.valueOf(months), 0, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
            BigDecimal principalPayment = basePrincipalPayment;

            if (i == months) {
                // 마지막 회차 단수차 조정: 정수 나눗셈으로 남은 잔액 전액을 흡수
                principalPayment = remainingBalance;
                remainingBalance = BigDecimal.ZERO;
            } else {
                remainingBalance = remainingBalance.subtract(principalPayment);
            }

            BigDecimal totalPayment = principalPayment.add(interestPayment);

            schedules.add(new ScheduleDto(
                    i, startDate.plusMonths(i),
                    principalPayment, interestPayment, totalPayment, remainingBalance
            ));
        }

        return schedules;
    }

    // 만기일시상환 (Bullet / Maturity Lump-sum)
    // 매회 이자만 납부, 마지막 회차에 원금 전액 + 마지막 이자 납부
    public static List<ScheduleDto> calculateBulkRepayment(
            BigDecimal principal,
            BigDecimal annualRate,
            int months,
            LocalDate startDate
    ) {
        List<ScheduleDto> schedules = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal interestPayment = principal.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);

        for (int i = 1; i <= months; i++) {
            BigDecimal principalPayment = (i == months) ? principal : BigDecimal.ZERO;
            BigDecimal remainingBalance = (i == months) ? BigDecimal.ZERO : principal;
            BigDecimal totalPayment = principalPayment.add(interestPayment);

            schedules.add(new ScheduleDto(
                    i, startDate.plusMonths(i),
                    principalPayment, interestPayment, totalPayment, remainingBalance
            ));
        }

        return schedules;
    }
}
