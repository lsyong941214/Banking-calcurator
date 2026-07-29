package com.example.code.repository;

import com.example.code.domain.ProductLoanTermRule;
import com.example.code.domain.ProductLoanTermRuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLoanTermRuleRepository extends JpaRepository<ProductLoanTermRule, ProductLoanTermRuleId> {

    List<ProductLoanTermRule> findAllByOrderByProductCodeAscSortOrderAsc();
}
