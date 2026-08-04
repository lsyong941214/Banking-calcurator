package com.example.ledger.controller;

import com.example.ledger.dto.LedgerAccountResponse;
import com.example.ledger.service.LedgerAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerAccountController {

    private final LedgerAccountService ledgerAccountService;

    public LedgerAccountController(LedgerAccountService ledgerAccountService) {
        this.ledgerAccountService = ledgerAccountService;
    }

    @GetMapping("/accounts")
    public List<LedgerAccountResponse> accounts(@RequestParam(required = false) String keyword) {
        return ledgerAccountService.search(keyword);
    }

    // 이자계산/원장조회 화면의 "계좌 검색" 팝업 전용 -- 고객번호/고객명/계좌상태로 AND 검색.
    @GetMapping("/accounts/search")
    public List<LedgerAccountResponse> searchForAccountPicker(
            @RequestParam(required = false) String custNo,
            @RequestParam(required = false) String custName,
            @RequestParam(required = false) String acctStatCd) {
        return ledgerAccountService.searchForAccountPicker(custNo, custName, acctStatCd);
    }
}
