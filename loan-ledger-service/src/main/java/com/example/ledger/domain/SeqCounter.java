package com.example.ledger.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

// 채번 카운터 -- (seqCd, seqDivCd)별 다음 일련번호. 실제 채번은 이 엔티티를 통해서가 아니라
// SeqCounterRepository.incrementAndGetNextSeq()의 원자적 UPSERT로 이뤄진다 -- 이 엔티티는
// JpaRepository가 요구하는 타입 정의 + 필요시 조회용으로만 쓴다.
@Entity
@Table(name = "seq_counter")
@IdClass(SeqCounterId.class)
public class SeqCounter {

    @Id
    private String seqCd;

    @Id
    private String seqDivCd;

    private Integer nextSeq;

    protected SeqCounter() {
    }

    public String getSeqCd() {
        return seqCd;
    }

    public String getSeqDivCd() {
        return seqDivCd;
    }

    public Integer getNextSeq() {
        return nextSeq;
    }
}
