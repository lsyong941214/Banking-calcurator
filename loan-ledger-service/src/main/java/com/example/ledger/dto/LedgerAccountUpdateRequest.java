package com.example.ledger.dto;

import java.math.BigDecimal;

// acctNo/acctSeqNo(PK)는 경로변수로 받고 변경 대상에서 제외한다 -- PK는 원장 개설 후 불변.
public record LedgerAccountUpdateRequest(
        String custNo,
        String custName,
        String acctStatCd,
        String itemCd,
        String applyNo,
        String approvalNo,
        BigDecimal loanLimitAmt,
        BigDecimal loanBalAmt,
        String newDt,
        String matDt,
        String nextIntPayDt,
        String nextRepayDt,
        String lastIntPayDt,
        String lastRepayDt,
        String deadlineLossDt,
        String monthlyIntPayDay,
        BigDecimal baseRate,
        BigDecimal addRate,
        BigDecimal applyRate,
        BigDecimal earlyRepayFeeRate,
        String repayMethodCd,
        String rateChangeTypeCd,
        String rateChangeCycle,
        String virtualAcctNo
) {
}
