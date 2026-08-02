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
}
