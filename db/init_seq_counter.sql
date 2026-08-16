-- 채번 카운터(seq_counter) 테이블 -- 채번룰코드(LONSEQ=계좌번호, CUSSEQ=고객번호 등) + 구분값
-- 별로 다음 일련번호를 원자적으로 관리한다. loan-ledger-service가
-- INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING 한 문장으로 증가시켜서, 이전
-- "해당 접두사로 시작하는 최댓값 조회 후 +1" 방식에 있던 동시성 레이스 컨디션(두 요청이
-- 동시에 같은 다음 번호를 읽어가는 문제)을 근본적으로 없앤다.
--
-- LONSEQ(계좌번호 채번)의 구분값은 과목코드(01/02/03)뿐이고 신규일자는 포함하지 않는다 --
-- 계좌번호 자체(YYYYMMDD+과목코드+일련번호)에 날짜가 이미 들어가 있어서, 일련번호가 하루
-- 단위로 리셋되지 않고 과목별로 계속 누적돼도 계좌번호 유일성은 그대로 보장된다. 오히려
-- 날짜를 구분값에 포함시키면 매일 새 (LONSEQ, 날짜+과목) 행이 쌓이는 구조라 테이블이
-- 끝없이 자라기만 하고 실익이 없다.
-- CUSSEQ(고객번호 채번)는 아직 실제로 호출하는 곳이 없다 -- 고객원장(cust_base, init_customer.sql)엔
-- 아직 등록/조회 API 자체가 없어서, 구분값(01=개인)만 미리 준비해뒀다. 나중에 고객 등록
-- 기능을 만들 때 이 카운터를 그대로 쓰면 된다.

CREATE TABLE seq_counter (
    seq_cd     VARCHAR(10) NOT NULL,
    seq_div_cd VARCHAR(10) NOT NULL,
    next_seq   INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (seq_cd, seq_div_cd)
);
COMMENT ON COLUMN seq_counter.seq_cd IS '채번룰코드: LONSEQ=계좌번호(대출계좌원장), CUSSEQ=고객번호(고객원장)';
COMMENT ON COLUMN seq_counter.seq_div_cd IS '채번룰코드별 구분값: LONSEQ=과목코드(01/02/03), CUSSEQ=고객구분코드(01=개인)';
COMMENT ON COLUMN seq_counter.next_seq IS '해당 (채번룰코드, 구분값)으로 마지막까지 발급된 일련번호 -- 다음 채번은 이 값 + 1';

INSERT INTO seq_counter (seq_cd, seq_div_cd, next_seq) VALUES
    ('LONSEQ', '01', 0),
    ('LONSEQ', '02', 0),
    ('LONSEQ', '03', 0),
    ('CUSSEQ', '01', 0);
