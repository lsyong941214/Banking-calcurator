package com.example.code.controller;

import com.example.code.dto.CodeItemResponse;
import com.example.code.dto.ProductOptionResponse;
import com.example.code.service.ProductOptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/codes")
public class CodeController {

    private final ProductOptionService productOptionService;

    public CodeController(ProductOptionService productOptionService) {
        this.productOptionService = productOptionService;
    }

    @GetMapping("/repayment-types")
    public List<CodeItemResponse> repaymentTypes() {
        return productOptionService.getRepaymentTypes();
    }

    @GetMapping("/product-options")
    public List<ProductOptionResponse> productOptions() {
        return productOptionService.getProductOptions();
    }
}
