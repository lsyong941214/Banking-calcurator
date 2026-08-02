package com.example.ledger.domain;

import java.io.Serializable;
import java.util.Objects;

public class LonAcctBaseId implements Serializable {

    private String acctNo;
    private Integer acctSeqNo;

    public LonAcctBaseId() {
    }

    public LonAcctBaseId(String acctNo, Integer acctSeqNo) {
        this.acctNo = acctNo;
        this.acctSeqNo = acctSeqNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LonAcctBaseId that)) return false;
        return Objects.equals(acctNo, that.acctNo) && Objects.equals(acctSeqNo, that.acctSeqNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctNo, acctSeqNo);
    }
}
