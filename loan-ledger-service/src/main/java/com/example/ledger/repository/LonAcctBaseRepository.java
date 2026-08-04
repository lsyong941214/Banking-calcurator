package com.example.ledger.repository;

import com.example.ledger.domain.LonAcctBase;
import com.example.ledger.domain.LonAcctBaseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LonAcctBaseRepository extends JpaRepository<LonAcctBase, LonAcctBaseId> {

    List<LonAcctBase> findAllByOrderByAcctNoAscAcctSeqNoAsc();

    List<LonAcctBase> findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(
            String acctNo, String custNo);

    // 계좌번호 검색 팝업 전용 -- 고객번호/고객명은 부분일치, 계좌상태는 정확히 일치, 모두 AND 조건.
    // 미입력 조건은 빈 문자열("")로 넘어오고, 빈 문자열은 항상 매칭되어 조건이 없는 것과 같다
    // (Postgres에서 null 바인드 파라미터를 LIKE에 섞으면 타입 추론 오류가 나서 null 대신 "" 사용).
    @Query("SELECT a FROM LonAcctBase a WHERE "
            + "a.custNo LIKE CONCAT('%', :custNo, '%') AND "
            + "a.custName LIKE CONCAT('%', :custName, '%') AND "
            + "(:acctStatCd = '' OR a.acctStatCd = :acctStatCd) "
            + "ORDER BY a.acctNo ASC, a.acctSeqNo ASC")
    List<LonAcctBase> searchForAccountPicker(
            @Param("custNo") String custNo,
            @Param("custName") String custName,
            @Param("acctStatCd") String acctStatCd);
}
