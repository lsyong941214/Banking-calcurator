package com.example.schedule.domain;

import java.io.Serializable;
import java.util.Objects;

public class ProductRepaymentTypeRuleId implements Serializable {

    private String productCode;
    private String repaymentTypeCode;

    public ProductRepaymentTypeRuleId() {
    }

    public ProductRepaymentTypeRuleId(String productCode, String repaymentTypeCode) {
        this.productCode = productCode;
        this.repaymentTypeCode = repaymentTypeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductRepaymentTypeRuleId that)) return false;
        return Objects.equals(productCode, that.productCode) && Objects.equals(repaymentTypeCode, that.repaymentTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCode, repaymentTypeCode);
    }
}
