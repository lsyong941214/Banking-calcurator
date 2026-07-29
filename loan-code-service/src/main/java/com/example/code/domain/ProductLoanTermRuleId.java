package com.example.code.domain;

import java.io.Serializable;
import java.util.Objects;

public class ProductLoanTermRuleId implements Serializable {

    private String productCode;
    private Integer loanTermYears;

    public ProductLoanTermRuleId() {
    }

    public ProductLoanTermRuleId(String productCode, Integer loanTermYears) {
        this.productCode = productCode;
        this.loanTermYears = loanTermYears;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductLoanTermRuleId that)) return false;
        return Objects.equals(productCode, that.productCode) && Objects.equals(loanTermYears, that.loanTermYears);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCode, loanTermYears);
    }
}
