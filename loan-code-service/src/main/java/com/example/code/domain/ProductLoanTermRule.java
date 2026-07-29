package com.example.code.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** 상품유형(PRODUCT_TYPE)별로 허용되는 대출기간(년). */
@Entity
@Table(name = "product_loan_term_rule")
@IdClass(ProductLoanTermRuleId.class)
public class ProductLoanTermRule {

    @Id
    private String productCode;

    @Id
    private Integer loanTermYears;

    private int sortOrder;

    protected ProductLoanTermRule() {
    }

    public String getProductCode() {
        return productCode;
    }

    public Integer getLoanTermYears() {
        return loanTermYears;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
