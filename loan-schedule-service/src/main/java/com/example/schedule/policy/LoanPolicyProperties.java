package com.example.schedule.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Placeholder policy values (industry-typical defaults, not a confirmed product policy).
 * Mirrors loan-engine-service's LoanPolicyProperties -- kept as a separate copy since the
 * two services don't share a library by design.
 */
@ConfigurationProperties(prefix = "loan.policy")
public class LoanPolicyProperties {

    /** 연체가산이율 (연체이율 = 적용금리 + 이 값). */
    private BigDecimal overdueRateAddOn = new BigDecimal("0.03");

    /** 일할계산 기준일수. */
    private int daysInYear = 365;

    public BigDecimal getOverdueRateAddOn() {
        return overdueRateAddOn;
    }

    public void setOverdueRateAddOn(BigDecimal overdueRateAddOn) {
        this.overdueRateAddOn = overdueRateAddOn;
    }

    public int getDaysInYear() {
        return daysInYear;
    }

    public void setDaysInYear(int daysInYear) {
        this.daysInYear = daysInYear;
    }
}
