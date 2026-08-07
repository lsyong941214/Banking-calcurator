package com.example.code.controller;

import com.example.code.dto.CodeItemResponse;
import com.example.code.dto.HolidayResponse;
import com.example.code.dto.ProductOptionResponse;
import com.example.code.service.HolidayService;
import com.example.code.service.ProductOptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/codes")
public class CodeController {

    private final ProductOptionService productOptionService;
    private final HolidayService holidayService;

    public CodeController(ProductOptionService productOptionService, HolidayService holidayService) {
        this.productOptionService = productOptionService;
        this.holidayService = holidayService;
    }

    @GetMapping("/repayment-types")
    public List<CodeItemResponse> repaymentTypes() {
        return productOptionService.getRepaymentTypes();
    }

    @GetMapping("/product-options")
    public List<ProductOptionResponse> productOptions() {
        return productOptionService.getProductOptions();
    }

    @GetMapping("/holidays")
    public List<HolidayResponse> holidays() {
        return holidayService.getHolidays();
    }
}
