package com.example.ledger.service;

import com.example.ledger.domain.LonAcctBase;
import com.example.ledger.domain.LonAcctBaseId;
import com.example.ledger.dto.LedgerAccountCreateRequest;
import com.example.ledger.dto.LedgerAccountResponse;
import com.example.ledger.dto.LedgerAccountUpdateRequest;
import com.example.ledger.repository.LonAcctBaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LedgerAccountService {

    private final LonAcctBaseRepository lonAcctBaseRepository;

    public LedgerAccountService(LonAcctBaseRepository lonAcctBaseRepository) {
        this.lonAcctBaseRepository = lonAcctBaseRepository;
    }

    public List<LedgerAccountResponse> search(String keyword) {
        List<LonAcctBase> accounts = StringUtils.hasText(keyword)
                ? lonAcctBaseRepository.findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(keyword, keyword)
                : lonAcctBaseRepository.findAllByOrderByAcctNoAscAcctSeqNoAsc();

        return accounts.stream().map(LedgerAccountResponse::from).toList();
    }

    // 계좌번호 검색 팝업(이자계산/원장조회 화면 공용) 전용 -- 고객번호/고객명/계좌상태로 AND 검색.
    public List<LedgerAccountResponse> searchForAccountPicker(String custNo, String custName, String acctStatCd) {
        return lonAcctBaseRepository.searchForAccountPicker(custNo, custName, acctStatCd)
                .stream().map(LedgerAccountResponse::from).toList();
    }

    public LedgerAccountResponse getOne(String acctNo, Integer acctSeqNo) {
        return LedgerAccountResponse.from(findOrThrow(acctNo, acctSeqNo));
    }

    public LedgerAccountResponse register(LedgerAccountCreateRequest request) {
        LonAcctBase account = new LonAcctBase(
                request.acctNo(), request.acctSeqNo(), request.custNo(), request.custName(),
                request.acctStatCd(), request.itemCd(), request.applyNo(), request.approvalNo(),
                request.loanLimitAmt(), request.loanBalAmt(), request.newDt(), request.matDt(),
                request.nextIntPayDt(), request.nextRepayDt(), request.lastIntPayDt(), request.lastRepayDt(),
                request.deadlineLossDt(), request.monthlyIntPayDay(), request.baseRate(), request.addRate(),
                request.applyRate(), request.earlyRepayFeeRate(), request.repayMethodCd(),
                request.rateChangeTypeCd(), request.rateChangeCycle(), request.virtualAcctNo());

        return LedgerAccountResponse.from(lonAcctBaseRepository.save(account));
    }

    public LedgerAccountResponse update(String acctNo, Integer acctSeqNo, LedgerAccountUpdateRequest request) {
        LonAcctBase account = findOrThrow(acctNo, acctSeqNo);

        account.setCustNo(request.custNo());
        account.setCustName(request.custName());
        account.setAcctStatCd(request.acctStatCd());
        account.setItemCd(request.itemCd());
        account.setApplyNo(request.applyNo());
        account.setApprovalNo(request.approvalNo());
        account.setLoanLimitAmt(request.loanLimitAmt());
        account.setLoanBalAmt(request.loanBalAmt());
        account.setNewDt(request.newDt());
        account.setMatDt(request.matDt());
        account.setNextIntPayDt(request.nextIntPayDt());
        account.setNextRepayDt(request.nextRepayDt());
        account.setLastIntPayDt(request.lastIntPayDt());
        account.setLastRepayDt(request.lastRepayDt());
        account.setDeadlineLossDt(request.deadlineLossDt());
        account.setMonthlyIntPayDay(request.monthlyIntPayDay());
        account.setBaseRate(request.baseRate());
        account.setAddRate(request.addRate());
        account.setApplyRate(request.applyRate());
        account.setEarlyRepayFeeRate(request.earlyRepayFeeRate());
        account.setRepayMethodCd(request.repayMethodCd());
        account.setRateChangeTypeCd(request.rateChangeTypeCd());
        account.setRateChangeCycle(request.rateChangeCycle());
        account.setVirtualAcctNo(request.virtualAcctNo());

        return LedgerAccountResponse.from(lonAcctBaseRepository.save(account));
    }

    public void delete(String acctNo, Integer acctSeqNo) {
        LonAcctBaseId id = new LonAcctBaseId(acctNo, acctSeqNo);
        if (!lonAcctBaseRepository.existsById(id)) {
            throw new NoSuchElementException("계좌를 찾을 수 없습니다: " + acctNo + "-" + acctSeqNo);
        }
        lonAcctBaseRepository.deleteById(id);
    }

    private LonAcctBase findOrThrow(String acctNo, Integer acctSeqNo) {
        return lonAcctBaseRepository.findById(new LonAcctBaseId(acctNo, acctSeqNo))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다: " + acctNo + "-" + acctSeqNo));
    }
}
