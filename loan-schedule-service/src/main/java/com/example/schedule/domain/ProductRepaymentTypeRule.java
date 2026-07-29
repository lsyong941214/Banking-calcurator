package com.example.schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** 상품유형(PRODUCT_TYPE)별로 허용되는 상환방식(REPAYMENT_TYPE). */
@Entity
@Table(name = "product_repayment_type_rule")
@IdClass(ProductRepaymentTypeRuleId.class)
public class ProductRepaymentTypeRule {

    @Id
    private String productCode;

    @Id
    private String repaymentTypeCode;

    private int sortOrder;

    protected ProductRepaymentTypeRule() {
    }

    public String getProductCode() {
        return productCode;
    }

    public String getRepaymentTypeCode() {
        return repaymentTypeCode;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
