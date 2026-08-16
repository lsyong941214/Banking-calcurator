package com.example.ledger.domain;

import java.io.Serializable;
import java.util.Objects;

public class SeqCounterId implements Serializable {

    private String seqCd;
    private String seqDivCd;

    public SeqCounterId() {
    }

    public SeqCounterId(String seqCd, String seqDivCd) {
        this.seqCd = seqCd;
        this.seqDivCd = seqDivCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeqCounterId that)) return false;
        return Objects.equals(seqCd, that.seqCd) && Objects.equals(seqDivCd, that.seqDivCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqCd, seqDivCd);
    }
}
