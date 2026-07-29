package com.example.schedule.repository;

import com.example.schedule.domain.ProductLoanTermRule;
import com.example.schedule.domain.ProductLoanTermRuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLoanTermRuleRepository extends JpaRepository<ProductLoanTermRule, ProductLoanTermRuleId> {

    List<ProductLoanTermRule> findAllByOrderByProductCodeAscSortOrderAsc();
}
