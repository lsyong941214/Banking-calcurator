-- 고객원장(cust_base) 테이블 -- code_group/code_item(init.sql), lon_acct_base(init_ledger.sql)와
-- 마찬가지로 마이그레이션 도구 없이(demo project) 이 파일을 직접 관리한다.
--
-- 아직 이 테이블을 소유하는 서비스/화면은 없다 (2026-08-16 기준) -- 대출계좌원장(lon_acct_base)에
-- 이미 존재하는 고객번호를 고객원장에 등록해두기 위한 초기 데이터 적재 요청으로 우선 테이블과
-- 데이터만 만든다. 어느 서비스가 이 테이블을 소유할지, lon_acct_base.cust_no에 FK를 걸지는
-- 아직 정해지지 않았다 (loan-ledger-service에 흡수할지, 별도 신규 모듈로 뺄지는 이 프로젝트의
-- "업무기능별로 서비스를 나눈다" 원칙에 따라 실제 조회/등록 API가 필요해질 때 다시 판단할 것).

CREATE TABLE cust_base (
    cust_no      VARCHAR(20)  NOT NULL,
    cust_name    VARCHAR(50)  NOT NULL,
    birth_dt     VARCHAR(8)   NOT NULL,
    cust_stat_cd VARCHAR(2)   NOT NULL,
    address      VARCHAR(200),
    PRIMARY KEY (cust_no),
    CONSTRAINT ck_cust_base_cust_stat_cd
        CHECK (cust_stat_cd IN ('01', '04', '09'))
);
COMMENT ON COLUMN cust_base.cust_stat_cd IS '01=정상, 04=해제, 09=휴면';
COMMENT ON COLUMN cust_base.birth_dt IS 'YYYYMMDD -- lon_acct_base의 날짜 컬럼과 동일한 관례(원장 시스템 정합성), SQL DATE 아님';

-- lon_acct_base에 이미 존재하는 고객번호를 등록한다. 같은 cust_no가 여러 행(회차)에 걸쳐 있는데
-- cust_name이 갈리는 경우를 대비해, acct_no/acct_seq_no 기준으로 가장 앞선(작은) 행의 cust_name
-- 으로 통일해 고객당 한 건만 저장한다 -- 현재 데이터엔 실제 충돌 사례는 없음(2024000078 두 행
-- 모두 "이서연"으로 동일). birth_dt/cust_stat_cd/address는 lon_acct_base에 없는 정보라
-- lon_acct_base에서 가져올 수 없다 -- 데모용으로 그럴듯한 값을 채워 넣었다(실제 개인정보 아님).
INSERT INTO cust_base (cust_no, cust_name, birth_dt, cust_stat_cd, address)
SELECT canonical.cust_no, canonical.cust_name, extra.birth_dt, extra.cust_stat_cd, extra.address
FROM (
    SELECT DISTINCT ON (cust_no) cust_no, cust_name
    FROM lon_acct_base
    ORDER BY cust_no, acct_no, acct_seq_no
) AS canonical
JOIN (
    VALUES
        ('2021000045', '19850312', '01', '서울특별시 강남구 테헤란로 123'),
        ('2024000078', '19920725', '01', '경기도 성남시 분당구 정자로 45'),
        ('2025000012', '19780904', '01', '부산광역시 해운대구 센텀중앙로 78'),
        ('2022000391', '19950130', '01', '인천광역시 연수구 송도과학로 12')
) AS extra(cust_no, birth_dt, cust_stat_cd, address)
ON extra.cust_no = canonical.cust_no;
