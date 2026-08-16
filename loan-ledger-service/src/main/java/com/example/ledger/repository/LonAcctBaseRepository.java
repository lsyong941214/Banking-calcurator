package com.example.ledger.repository;

import com.example.ledger.domain.LonAcctBase;
import com.example.ledger.domain.LonAcctBaseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LonAcctBaseRepository extends JpaRepository<LonAcctBase, LonAcctBaseId>, LonAcctBaseRepositoryCustom {

    List<LonAcctBase> findAllByOrderByAcctNoAscAcctSeqNoAsc();

    List<LonAcctBase> findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(
            String acctNo, String custNo);

    // 계좌번호 채번(신규일자+과목코드 접두 하에서 가장 큰 번호) 전용 -- LedgerAccountService.generateAcctNo 참고.
    Optional<LonAcctBase> findFirstByAcctNoStartingWithOrderByAcctNoDesc(String acctNoPrefix);
}
