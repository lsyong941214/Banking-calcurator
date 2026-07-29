package com.example.schedule.repository;

import com.example.schedule.domain.ProductRepaymentTypeRule;
import com.example.schedule.domain.ProductRepaymentTypeRuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepaymentTypeRuleRepository extends JpaRepository<ProductRepaymentTypeRule, ProductRepaymentTypeRuleId> {

    List<ProductRepaymentTypeRule> findAllByOrderByProductCodeAscSortOrderAsc();
}
