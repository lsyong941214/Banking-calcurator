package com.example.ledger.dto;

import java.math.BigDecimal;

// 계좌생성 화면의 "등록" 처리 전용 -- 신규 원장 개설에 실제로 입력받는 항목만 담는다.
// acctNo/acctSeqNo(채번), acctStatCd/loanBalAmt/lastIntPayDt/lastRepayDt/nextIntPayDt/
// nextRepayDt/deadlineLossDt(자동 설정)는 LedgerAccountService.register()가 계산해 채운다.
public record LedgerAccountCreateRequest(
        String custNo,
        String custName,
        String itemCd,
        BigDecimal loanLimitAmt,
        String newDt,
        String matDt,
        String monthlyIntPayDay,
        BigDecimal baseRate,
        BigDecimal addRate,
        BigDecimal applyRate,
        BigDecimal earlyRepayFeeRate,
        String repayMethodCd,
        String rateChangeTypeCd,
        String rateChangeCycle
) {
}
