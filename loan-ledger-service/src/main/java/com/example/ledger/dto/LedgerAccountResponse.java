package com.example.ledger.dto;

import com.example.ledger.domain.LonAcctBase;

import java.math.BigDecimal;

public record LedgerAccountResponse(
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
    public static LedgerAccountResponse from(LonAcctBase a) {
        return new LedgerAccountResponse(
                a.getAcctNo(), a.getAcctSeqNo(), a.getCustNo(), a.getCustName(), a.getAcctStatCd(), a.getItemCd(),
                a.getApplyNo(), a.getApprovalNo(), a.getLoanLimitAmt(), a.getLoanBalAmt(),
                a.getNewDt(), a.getMatDt(), a.getNextIntPayDt(), a.getNextRepayDt(),
                a.getLastIntPayDt(), a.getLastRepayDt(), a.getDeadlineLossDt(), a.getMonthlyIntPayDay(),
                a.getBaseRate(), a.getAddRate(), a.getApplyRate(), a.getEarlyRepayFeeRate(),
                a.getRepayMethodCd(), a.getRateChangeTypeCd(), a.getRateChangeCycle(), a.getVirtualAcctNo()
        );
    }
}
