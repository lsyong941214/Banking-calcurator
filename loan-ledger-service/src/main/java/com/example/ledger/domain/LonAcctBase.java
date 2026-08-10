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
    private String custName;
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
    private String rateChangeTypeCd;
    private String rateChangeCycle;
    private String virtualAcctNo;

    protected LonAcctBase() {
    }

    public LonAcctBase(String acctNo, Integer acctSeqNo, String custNo, String custName, String acctStatCd,
            String itemCd, String applyNo, String approvalNo, BigDecimal loanLimitAmt, BigDecimal loanBalAmt,
            String newDt, String matDt, String nextIntPayDt, String nextRepayDt, String lastIntPayDt,
            String lastRepayDt, String deadlineLossDt, String monthlyIntPayDay, BigDecimal baseRate,
            BigDecimal addRate, BigDecimal applyRate, BigDecimal earlyRepayFeeRate, String repayMethodCd,
            String rateChangeTypeCd, String rateChangeCycle, String virtualAcctNo) {
        this.acctNo = acctNo;
        this.acctSeqNo = acctSeqNo;
        this.custNo = custNo;
        this.custName = custName;
        this.acctStatCd = acctStatCd;
        this.itemCd = itemCd;
        this.applyNo = applyNo;
        this.approvalNo = approvalNo;
        this.loanLimitAmt = loanLimitAmt;
        this.loanBalAmt = loanBalAmt;
        this.newDt = newDt;
        this.matDt = matDt;
        this.nextIntPayDt = nextIntPayDt;
        this.nextRepayDt = nextRepayDt;
        this.lastIntPayDt = lastIntPayDt;
        this.lastRepayDt = lastRepayDt;
        this.deadlineLossDt = deadlineLossDt;
        this.monthlyIntPayDay = monthlyIntPayDay;
        this.baseRate = baseRate;
        this.addRate = addRate;
        this.applyRate = applyRate;
        this.earlyRepayFeeRate = earlyRepayFeeRate;
        this.repayMethodCd = repayMethodCd;
        this.rateChangeTypeCd = rateChangeTypeCd;
        this.rateChangeCycle = rateChangeCycle;
        this.virtualAcctNo = virtualAcctNo;
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

    public String getCustName() {
        return custName;
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

    public String getRateChangeTypeCd() {
        return rateChangeTypeCd;
    }

    public String getRateChangeCycle() {
        return rateChangeCycle;
    }

    public String getVirtualAcctNo() {
        return virtualAcctNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public void setAcctStatCd(String acctStatCd) {
        this.acctStatCd = acctStatCd;
    }

    public void setItemCd(String itemCd) {
        this.itemCd = itemCd;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public void setApprovalNo(String approvalNo) {
        this.approvalNo = approvalNo;
    }

    public void setLoanLimitAmt(BigDecimal loanLimitAmt) {
        this.loanLimitAmt = loanLimitAmt;
    }

    public void setLoanBalAmt(BigDecimal loanBalAmt) {
        this.loanBalAmt = loanBalAmt;
    }

    public void setNewDt(String newDt) {
        this.newDt = newDt;
    }

    public void setMatDt(String matDt) {
        this.matDt = matDt;
    }

    public void setNextIntPayDt(String nextIntPayDt) {
        this.nextIntPayDt = nextIntPayDt;
    }

    public void setNextRepayDt(String nextRepayDt) {
        this.nextRepayDt = nextRepayDt;
    }

    public void setLastIntPayDt(String lastIntPayDt) {
        this.lastIntPayDt = lastIntPayDt;
    }

    public void setLastRepayDt(String lastRepayDt) {
        this.lastRepayDt = lastRepayDt;
    }

    public void setDeadlineLossDt(String deadlineLossDt) {
        this.deadlineLossDt = deadlineLossDt;
    }

    public void setMonthlyIntPayDay(String monthlyIntPayDay) {
        this.monthlyIntPayDay = monthlyIntPayDay;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

    public void setAddRate(BigDecimal addRate) {
        this.addRate = addRate;
    }

    public void setApplyRate(BigDecimal applyRate) {
        this.applyRate = applyRate;
    }

    public void setEarlyRepayFeeRate(BigDecimal earlyRepayFeeRate) {
        this.earlyRepayFeeRate = earlyRepayFeeRate;
    }

    public void setRepayMethodCd(String repayMethodCd) {
        this.repayMethodCd = repayMethodCd;
    }

    public void setRateChangeTypeCd(String rateChangeTypeCd) {
        this.rateChangeTypeCd = rateChangeTypeCd;
    }

    public void setRateChangeCycle(String rateChangeCycle) {
        this.rateChangeCycle = rateChangeCycle;
    }

    public void setVirtualAcctNo(String virtualAcctNo) {
        this.virtualAcctNo = virtualAcctNo;
    }
}
