-- 대출계좌원장(lon_acct_base) 테이블, loan-ledger-service가 소유한다.
-- code_group/code_item 등 loan-code-service 소유 테이블(init.sql)과 마찬가지로 마이그레이션
-- 도구 없이(demo project) 이 파일을 직접 관리한다 -- 스키마가 바뀌면 로컬은 볼륨을 지우고
-- 재적용, Supabase는 이 파일 내용을 그대로 재실행(또는 diff를 손으로 적용)한다.
--
-- PK가 acct_no 단독이 아니라 (acct_no, acct_seq_no) 복합키인 이유: 중도금대출처럼 한 계좌번호
-- 아래 여러 회차가 각각 별도로 기표되는 경우가 있어 acct_no만으로는 유니크하지 않다.

CREATE TABLE lon_acct_base (
    acct_no              VARCHAR(20)   NOT NULL,
    acct_seq_no          NUMERIC(3,0)  NOT NULL,
    cust_no              VARCHAR(20)   NOT NULL,
    cust_name            VARCHAR(50)   NOT NULL,
    acct_stat_cd         VARCHAR(2)    NOT NULL,
    item_cd              VARCHAR(2)    NOT NULL,
    apply_no             VARCHAR(20),
    approval_no          VARCHAR(20),
    loan_limit_amt       NUMERIC(15,0) NOT NULL,
    loan_bal_amt         NUMERIC(15,0) NOT NULL,
    new_dt               VARCHAR(8)    NOT NULL,
    mat_dt               VARCHAR(8)    NOT NULL,
    next_int_pay_dt      VARCHAR(8)    NOT NULL,
    next_repay_dt        VARCHAR(8)    NOT NULL,
    last_int_pay_dt      VARCHAR(8)    NOT NULL,
    last_repay_dt        VARCHAR(8)    NOT NULL,
    deadline_loss_dt     VARCHAR(8),
    monthly_int_pay_day  VARCHAR(2)    NOT NULL,
    base_rate            NUMERIC(8,6)  NOT NULL,
    add_rate             NUMERIC(8,6)  NOT NULL,
    apply_rate           NUMERIC(8,6)  NOT NULL,
    early_repay_fee_rate NUMERIC(8,6)  NOT NULL,
    repay_method_cd      VARCHAR(2)    NOT NULL,
    rate_change_type_cd  VARCHAR(2)    NOT NULL,
    rate_change_cycle    VARCHAR(2),
    virtual_acct_no      VARCHAR(20),
    PRIMARY KEY (acct_no, acct_seq_no),
    CONSTRAINT ck_lon_acct_base_acct_stat_cd
        CHECK (acct_stat_cd IN ('01', '03', '04', '05', '09')),
    CONSTRAINT ck_lon_acct_base_item_cd
        CHECK (item_cd IN ('01', '02', '03')),
    CONSTRAINT ck_lon_acct_base_repay_method_cd
        CHECK (repay_method_cd IN ('01', '02', '03')),
    CONSTRAINT ck_lon_acct_base_rate_change_type_cd
        CHECK (rate_change_type_cd IN ('01', '02')),
    -- 고정금리(01)는 변동주기가 없어야(NULL) 하고, 변동금리(02)는 반드시 주기가 있어야 한다.
    CONSTRAINT ck_lon_acct_base_rate_change_cycle
        CHECK ((rate_change_type_cd = '01' AND rate_change_cycle IS NULL)
            OR (rate_change_type_cd = '02' AND rate_change_cycle IS NOT NULL))
);

COMMENT ON TABLE lon_acct_base IS '대출계좌원장 -- 계좌번호+계좌일련번호 단위로 대출 조건/잔액/기일을 관리';
COMMENT ON COLUMN lon_acct_base.acct_no IS '계좌번호. 채번규칙: 신규년도(4)+신규일자(4,MMDD)+과목코드(2)+일련번호(N)';
COMMENT ON COLUMN lon_acct_base.acct_seq_no IS '계좌일련번호. 중도금대출 등 회차별 기표관리용 -- acct_no만으로는 유니크하지 않음';
COMMENT ON COLUMN lon_acct_base.cust_no IS '고객번호. 채번규칙: 신규년도+일련번호';
COMMENT ON COLUMN lon_acct_base.cust_name IS '고객명';
COMMENT ON COLUMN lon_acct_base.acct_stat_cd IS '계좌상태코드: 01-정상, 03-정리, 04-해지, 05-상각, 09-매각';
COMMENT ON COLUMN lon_acct_base.item_cd IS '과목코드: 01-주택담보대출, 02-전세자금대출, 03-신용대출';
COMMENT ON COLUMN lon_acct_base.apply_no IS '신청번호 (현재 미사용, NULL)';
COMMENT ON COLUMN lon_acct_base.approval_no IS '승인번호 (현재 미사용, NULL)';
COMMENT ON COLUMN lon_acct_base.loan_limit_amt IS '대출한도금액 (원)';
COMMENT ON COLUMN lon_acct_base.loan_bal_amt IS '대출잔액 (원)';
COMMENT ON COLUMN lon_acct_base.new_dt IS '신규일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.mat_dt IS '만기일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.next_int_pay_dt IS '다음이자납입일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.next_repay_dt IS '다음상환일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.last_int_pay_dt IS '최종이자납입일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.last_repay_dt IS '최종상환일자 (YYYYMMDD)';
COMMENT ON COLUMN lon_acct_base.deadline_loss_dt IS '기한이익상실일자 (YYYYMMDD, 해당 없으면 NULL)';
COMMENT ON COLUMN lon_acct_base.monthly_int_pay_day IS '매월이자납입일 (일, "01"~"31")';
COMMENT ON COLUMN lon_acct_base.base_rate IS '기준금리 (소수, 예: 0.035000 = 3.5%)';
COMMENT ON COLUMN lon_acct_base.add_rate IS '가산금리 (소수)';
COMMENT ON COLUMN lon_acct_base.apply_rate IS '적용금리 = 기준금리+가산금리 (소수)';
COMMENT ON COLUMN lon_acct_base.early_repay_fee_rate IS '조기상환수수료율 (소수)';
COMMENT ON COLUMN lon_acct_base.repay_method_cd IS '상환방식코드: 01-원리금균등, 02-원금균등, 03-만기일시';
COMMENT ON COLUMN lon_acct_base.rate_change_type_cd IS '금리변동구분코드: 01-고정금리, 02-변동금리';
COMMENT ON COLUMN lon_acct_base.rate_change_cycle IS '금리변동주기 (개월, "03"/"06"/"12" 등). 고정금리(01)면 NULL, 변동금리(02)면 필수';
COMMENT ON COLUMN lon_acct_base.virtual_acct_no IS '가상계좌번호 (현재 미사용, NULL)';

-- 테스트 데이터 5건.
-- 1건은 일반 주택담보대출, 2/3번째 행은 "같은 acct_no, 다른 acct_seq_no"로 중도금대출의
-- 회차별 기표(복합키가 필요한 이유)를 그대로 보여준다. 아직 발생하지 않은 이력성 일자
-- (예: 만기일시상환 계좌의 최종상환일자, 갓 해지된 계좌의 다음납입일자)는 NOT NULL 제약을
-- 지키기 위해 신규일자/만기일자 등 의미상 가장 가까운 날짜를 baseline 값으로 채웠다.
-- 금리변동구분/주기: 변동금리 계좌는 3~6개월 주기로 섞어서, 고정금리 계좌(4/5번)는 주기 NULL로 넣었다.
INSERT INTO lon_acct_base (
    acct_no, acct_seq_no, cust_no, cust_name, acct_stat_cd, item_cd, apply_no, approval_no,
    loan_limit_amt, loan_bal_amt, new_dt, mat_dt,
    next_int_pay_dt, next_repay_dt, last_int_pay_dt, last_repay_dt, deadline_loss_dt,
    monthly_int_pay_day, base_rate, add_rate, apply_rate, early_repay_fee_rate,
    repay_method_cd, rate_change_type_cd, rate_change_cycle, virtual_acct_no
) VALUES
    -- 1) 주택담보대출, 원리금균등, 정상 진행중, 변동금리(6개월 주기)
    ('20210610010001', 1, '2021000045', '김민준', '01', '01', NULL, NULL,
     600000000, 512340000, '20210610', '20510610',
     '20260810', '20260810', '20260710', '20260710', NULL,
     '10', 0.032000, 0.009000, 0.041000, 0.012000,
     '01', '02', '06', NULL),

    -- 2) 중도금대출 1차 실행분 -- 아래 3)과 acct_no가 동일, acct_seq_no로만 구분됨, 변동금리(3개월 주기)
    ('20240301010005', 1, '2024000078', '이서연', '01', '01', NULL, NULL,
     400000000, 150000000, '20240301', '20280301',
     '20260901', '20260901', '20260801', '20260801', NULL,
     '01', 0.034000, 0.010000, 0.044000, 0.015000,
     '01', '02', '03', NULL),

    -- 3) 중도금대출 2차 실행분 -- 2)와 같은 acct_no("20240301010005"), acct_seq_no만 2로 다름, 변동금리(3개월 주기)
    ('20240301010005', 2, '2024000078', '이서연', '01', '01', NULL, NULL,
     400000000, 140000000, '20240901', '20280301',
     '20260901', '20260901', '20260801', '20260801', NULL,
     '01', 0.036000, 0.010000, 0.046000, 0.015000,
     '01', '02', '03', NULL),

    -- 4) 전세자금대출, 만기일시상환 -- 만기 전까지 원금상환 이력이 없어 last_repay_dt는 신규일자로 baseline. 고정금리
    ('20250815020002', 1, '2025000012', '박지훈', '01', '02', NULL, NULL,
     300000000, 300000000, '20250815', '20270815',
     '20260815', '20270815', '20260715', '20250815', NULL,
     '15', 0.038000, 0.006000, 0.044000, 0.010000,
     '03', '01', NULL, NULL),

    -- 5) 신용대출, 원금균등, 만기 도래 후 정상 해지 (잔액 0). 고정금리
    ('20220920030007', 1, '2022000391', '최유리', '04', '03', NULL, NULL,
     50000000, 0, '20220920', '20240920',
     '20240920', '20240920', '20240920', '20240920', NULL,
     '20', 0.045000, 0.015000, 0.060000, 0.005000,
     '02', '01', NULL, NULL);
