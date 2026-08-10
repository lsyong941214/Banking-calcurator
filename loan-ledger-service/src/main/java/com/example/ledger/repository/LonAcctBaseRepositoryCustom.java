package com.example.ledger.repository;

import com.example.ledger.domain.LonAcctBase;

import java.util.List;

public interface LonAcctBaseRepositoryCustom {

    // 계좌번호 검색 팝업 전용 -- 고객번호/고객명은 부분일치, 계좌상태는 정확히 일치, 모두 AND 조건.
    // 미입력 조건(null/빈 문자열)은 predicate 자체를 생략해 조건 없음으로 취급한다.
    List<LonAcctBase> searchForAccountPicker(String custNo, String custName, String acctStatCd);
}
