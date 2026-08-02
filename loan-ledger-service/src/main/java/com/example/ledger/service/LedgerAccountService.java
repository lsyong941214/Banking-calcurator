package com.example.ledger.service;

import com.example.ledger.dto.LedgerAccountResponse;
import com.example.ledger.repository.LonAcctBaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LedgerAccountService {

    private final LonAcctBaseRepository lonAcctBaseRepository;

    public LedgerAccountService(LonAcctBaseRepository lonAcctBaseRepository) {
        this.lonAcctBaseRepository = lonAcctBaseRepository;
    }

    public List<LedgerAccountResponse> search(String keyword) {
        List<com.example.ledger.domain.LonAcctBase> accounts = StringUtils.hasText(keyword)
                ? lonAcctBaseRepository.findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(keyword, keyword)
                : lonAcctBaseRepository.findAllByOrderByAcctNoAscAcctSeqNoAsc();

        return accounts.stream().map(LedgerAccountResponse::from).toList();
    }
}
