package com.example.ledger.repository;

import com.example.ledger.domain.LonAcctBase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.ledger.domain.QLonAcctBase.lonAcctBase;

public class LonAcctBaseRepositoryImpl implements LonAcctBaseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public LonAcctBaseRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<LonAcctBase> searchForAccountPicker(String custNo, String custName, String acctStatCd) {
        BooleanBuilder where = new BooleanBuilder();
        if (StringUtils.hasText(custNo)) {
            where.and(lonAcctBase.custNo.contains(custNo));
        }
        if (StringUtils.hasText(custName)) {
            where.and(lonAcctBase.custName.contains(custName));
        }
        if (StringUtils.hasText(acctStatCd)) {
            where.and(lonAcctBase.acctStatCd.eq(acctStatCd));
        }

        return queryFactory.selectFrom(lonAcctBase)
                .where(where)
                .orderBy(lonAcctBase.acctNo.asc(), lonAcctBase.acctSeqNo.asc())
                .fetch();
    }
}
