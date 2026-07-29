package com.example.schedule.controller;

import com.example.schedule.dto.CodeItemResponse;
import com.example.schedule.dto.ProductOptionResponse;
import com.example.schedule.service.ProductOptionService;
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
