package com.example.code.service;

import com.example.code.domain.CodeItem;
import com.example.code.domain.ProductLoanTermRule;
import com.example.code.domain.ProductRepaymentTypeRule;
import com.example.code.dto.CodeItemResponse;
import com.example.code.dto.ProductOptionResponse;
import com.example.code.repository.CodeItemRepository;
import com.example.code.repository.ProductLoanTermRuleRepository;
import com.example.code.repository.ProductRepaymentTypeRuleRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductOptionService {

    private static final String PRODUCT_TYPE_GROUP = "PRODUCT_TYPE";
    private static final String REPAYMENT_TYPE_GROUP = "REPAYMENT_TYPE";

    private final CodeItemRepository codeItemRepository;
    private final ProductRepaymentTypeRuleRepository repaymentTypeRuleRepository;
    private final ProductLoanTermRuleRepository loanTermRuleRepository;

    public ProductOptionService(
            CodeItemRepository codeItemRepository,
            ProductRepaymentTypeRuleRepository repaymentTypeRuleRepository,
            ProductLoanTermRuleRepository loanTermRuleRepository
    ) {
        this.codeItemRepository = codeItemRepository;
        this.repaymentTypeRuleRepository = repaymentTypeRuleRepository;
        this.loanTermRuleRepository = loanTermRuleRepository;
    }

    public List<CodeItemResponse> getRepaymentTypes() {
        return codeItemRepository.findByGroupCodeOrderBySortOrder(REPAYMENT_TYPE_GROUP).stream()
                .map(item -> new CodeItemResponse(item.getCode(), item.getCodeName()))
                .toList();
    }

    public List<ProductOptionResponse> getProductOptions() {
        List<CodeItem> productTypes = codeItemRepository.findByGroupCodeOrderBySortOrder(PRODUCT_TYPE_GROUP);

        Map<String, List<String>> allowedRepaymentTypesByProduct = groupOrdered(
                repaymentTypeRuleRepository.findAllByOrderByProductCodeAscSortOrderAsc(),
                ProductRepaymentTypeRule::getProductCode,
                ProductRepaymentTypeRule::getRepaymentTypeCode
        );
        Map<String, List<Integer>> allowedLoanTermsByProduct = groupOrdered(
                loanTermRuleRepository.findAllByOrderByProductCodeAscSortOrderAsc(),
                ProductLoanTermRule::getProductCode,
                ProductLoanTermRule::getLoanTermYears
        );

        return productTypes.stream()
                .map(product -> new ProductOptionResponse(
                        product.getCode(),
                        product.getCodeName(),
                        allowedRepaymentTypesByProduct.getOrDefault(product.getCode(), List.of()),
                        allowedLoanTermsByProduct.getOrDefault(product.getCode(), List.of())
                ))
                .toList();
    }

    private <T, V> Map<String, List<V>> groupOrdered(
            List<T> rows, java.util.function.Function<T, String> keyFn, java.util.function.Function<T, V> valueFn
    ) {
        Map<String, List<V>> result = new LinkedHashMap<>();
        for (T row : rows) {
            result.computeIfAbsent(keyFn.apply(row), k -> new java.util.ArrayList<>()).add(valueFn.apply(row));
        }
        return result;
    }
}
