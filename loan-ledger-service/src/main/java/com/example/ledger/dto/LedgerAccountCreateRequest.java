package com.example.ledger.dto;

import java.math.BigDecimal;

public record LedgerAccountCreateRequest(
        String acctNo,
        Integer acctSeqNo,
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
