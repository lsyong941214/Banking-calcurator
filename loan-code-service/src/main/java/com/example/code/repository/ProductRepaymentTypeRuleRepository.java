package com.example.code.repository;

import com.example.code.domain.ProductRepaymentTypeRule;
import com.example.code.domain.ProductRepaymentTypeRuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepaymentTypeRuleRepository extends JpaRepository<ProductRepaymentTypeRule, ProductRepaymentTypeRuleId> {

    List<ProductRepaymentTypeRule> findAllByOrderByProductCodeAscSortOrderAsc();
}
