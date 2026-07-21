package com.example.engine.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Placeholder policy values (industry-typical defaults, not a confirmed product policy).
 * Adjust in application.properties once actual bank policy is confirmed.
 */
@ConfigurationProperties(prefix = "loan.policy")
public class LoanPolicyProperties {

    /** 연체가산이율 (연체이율 = 적용금리 + 이 값). */
    private BigDecimal overdueRateAddOn = new BigDecimal("0.03");

    /** 중도상환수수료율 (화면에서 직접 입력/면제 가능; 값을 안 넘기면 이 기본값을 사용). */
    private BigDecimal earlyRepaymentFeeRate = new BigDecimal("0.01");

    /** 일할계산 기준일수. */
    private int daysInYear = 365;

    public BigDecimal getOverdueRateAddOn() {
        return overdueRateAddOn;
    }

    public void setOverdueRateAddOn(BigDecimal overdueRateAddOn) {
        this.overdueRateAddOn = overdueRateAddOn;
    }

    public BigDecimal getEarlyRepaymentFeeRate() {
        return earlyRepaymentFeeRate;
    }

    public void setEarlyRepaymentFeeRate(BigDecimal earlyRepaymentFeeRate) {
        this.earlyRepaymentFeeRate = earlyRepaymentFeeRate;
    }

    public int getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(int daysInYear) {
        this.daysInYear = daysInYear;
    }
}
