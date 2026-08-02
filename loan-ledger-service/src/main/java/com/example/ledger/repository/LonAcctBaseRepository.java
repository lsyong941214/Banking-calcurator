package com.example.ledger.repository;

import com.example.ledger.domain.LonAcctBase;
import com.example.ledger.domain.LonAcctBaseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LonAcctBaseRepository extends JpaRepository<LonAcctBase, LonAcctBaseId> {

    List<LonAcctBase> findAllByOrderByAcctNoAscAcctSeqNoAsc();

    List<LonAcctBase> findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(
            String acctNo, String custNo);
}
