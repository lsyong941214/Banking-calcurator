package com.example.ledger.repository;

import com.example.ledger.domain.SeqCounter;
import com.example.ledger.domain.SeqCounterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeqCounterRepository extends JpaRepository<SeqCounter, SeqCounterId> {

    // (seqCd, seqDivCd) 행을 원자적으로 +1(없으면 1부터 생성)하고 갱신된 값을 반환한다.
    // INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING은 단일 SQL 문장으로 실행되는
    // 원자적 연산이라, "조회 후 계산해서 다시 저장" 방식과 달리 두 요청이 동시에 같은
    // (seqCd, seqDivCd)로 들어와도 서로 다른 값을 받는다 -- Postgres가 행 잠금을 보장한다.
    @Query(value = """
            INSERT INTO seq_counter (seq_cd, seq_div_cd, next_seq)
            VALUES (:seqCd, :seqDivCd, 1)
            ON CONFLICT (seq_cd, seq_div_cd)
            DO UPDATE SET next_seq = seq_counter.next_seq + 1
            RETURNING next_seq
            """, nativeQuery = true)
    int incrementAndGetNextSeq(@Param("seqCd") String seqCd, @Param("seqDivCd") String seqDivCd);
}
