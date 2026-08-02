package com.example.ledger.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "lon_acct_base")
@IdClass(LonAcctBaseId.class)
public class LonAcctBase {

    @Id
    private String acctNo;

    @Id
    private Integer acctSeqNo;

    private String custNo;
    private String acctStatCd;
    private String itemCd;
    private String applyNo;
    private String approvalNo;
    private BigDecimal loanLimitAmt;
    private BigDecimal loanBalAmt;
    private String newDt;
    private String matDt;
    private String nextIntPayDt;
    private String nextRepayDt;
    private String lastIntPayDt;
    private String lastRepayDt;
    private String deadlineLossDt;
    private String monthlyIntPayDay;
    private BigDecimal baseRate;
    private BigDecimal addRate;
    private BigDecimal applyRate;
    private BigDecimal earlyRepayFeeRate;
    private String repayMethodCd;
    private String virtualAcctNo;

    protected LonAcctBase() {
    }

    public String getAcctNo() {
        return acctNo;
    }

    public Integer getAcctSeqNo() {
        return acctSeqNo;
    }

    public String getCustNo() {
        return custNo;
    }

    public String getAcctStatCd() {
        return acctStatCd;
    }

    public String getItemCd() {
        return itemCd;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public String getApprovalNo() {
        return approvalNo;
    }

    public BigDecimal getLoanLimitAmt() {
        return loanLimitAmt;
    }

    public BigDecimal getLoanBalAmt() {
        return loanBalAmt;
    }

    public String getNewDt() {
        return newDt;
    }

    public String getMatDt() {
        return matDt;
    }

    public String getNextIntPayDt() {
        return nextIntPayDt;
    }

    public String getNextRepayDt() {
        return nextRepayDt;
    }

    public String getLastIntPayDt() {
        return lastIntPayDt;
    }

    public String getLastRepayDt() {
        return lastRepayDt;
    }

    public String getDeadlineLossDt() {
        return deadlineLossDt;
    }

    public String getMonthlyIntPayDay() {
        return monthlyIntPayDay;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public BigDecimal getAddRate() {
        return addRate;
    }

    public BigDecimal getApplyRate() {
        return applyRate;
    }

    public BigDecimal getEarlyRepayFeeRate() {
        return earlyRepayFeeRate;
    }

    public String getRepayMethodCd() {
        return repayMethodCd;
    }

    public String getVirtualAcctNo() {
        return virtualAcctNo;
    }
}
