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

    // 계좌번호 검색 팝업(이자계산/원장조회 화면 공용) 전용 -- 고객번호/고객명/계좌상태로 AND 검색.
    public List<LedgerAccountResponse> searchForAccountPicker(String custNo, String custName, String acctStatCd) {
        List<com.example.ledger.domain.LonAcctBase> accounts = lonAcctBaseRepository.searchForAccountPicker(
                StringUtils.hasText(custNo) ? custNo : "",
                StringUtils.hasText(custName) ? custName : "",
                StringUtils.hasText(acctStatCd) ? acctStatCd : "");

        return accounts.stream().map(LedgerAccountResponse::from).toList();
    }
}
