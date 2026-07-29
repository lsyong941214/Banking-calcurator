-- Code tables owned by loan-code-service.
-- Applied once when the postgres container's data volume is first created
-- (docker-entrypoint-initdb.d). No migration tool yet (demo project) -- if the
-- schema changes, drop the volume and let this file re-run, or apply the diff
-- by hand. Ledger tables (loan_contract / repayment_schedule) are deferred --
-- which service will own them is still undecided, see
-- .claude/skills/banking-loan-calculator/SKILL.md.

CREATE TABLE code_group (
    group_code VARCHAR(50) PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL
);

CREATE TABLE code_item (
    group_code VARCHAR(50) NOT NULL REFERENCES code_group(group_code),
    code       VARCHAR(50) NOT NULL,
    code_name  VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (group_code, code)
);

-- 상품유형(PRODUCT_TYPE)별로 허용되는 상환방식(REPAYMENT_TYPE). 데모 규모라 code_item에
-- 대한 정식 FK는 생략하고 애플리케이션/시드 데이터 수준에서 정합성을 맞춘다.
CREATE TABLE product_repayment_type_rule (
    product_code         VARCHAR(50) NOT NULL,
    repayment_type_code  VARCHAR(50) NOT NULL,
    sort_order           INT NOT NULL DEFAULT 0,
    PRIMARY KEY (product_code, repayment_type_code)
);

-- 상품유형(PRODUCT_TYPE)별로 허용되는 대출기간(년).
CREATE TABLE product_loan_term_rule (
    product_code    VARCHAR(50) NOT NULL,
    loan_term_years INT NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    PRIMARY KEY (product_code, loan_term_years)
);

INSERT INTO code_group (group_code, group_name) VALUES
    ('PRODUCT_TYPE', '상품유형'),
    ('REPAYMENT_TYPE', '상환방식');

INSERT INTO code_item (group_code, code, code_name, sort_order) VALUES
    ('PRODUCT_TYPE', '01', '주택담보대출', 1),
    ('PRODUCT_TYPE', '02', '전세자금대출', 2),
    ('PRODUCT_TYPE', '03', '신용대출', 3),
    ('REPAYMENT_TYPE', 'EQUAL_PRINCIPAL_AND_INTEREST', '원리금균등상환', 1),
    ('REPAYMENT_TYPE', 'EQUAL_PRINCIPAL', '원금균등상환', 2),
    ('REPAYMENT_TYPE', 'BULK', '만기일시상환', 3);

-- 주택담보대출(01): 만기일시상환 불가 / 전세자금대출(02): 만기일시상환만 가능 / 신용대출(03): 전체 가능
INSERT INTO product_repayment_type_rule (product_code, repayment_type_code, sort_order) VALUES
    ('01', 'EQUAL_PRINCIPAL_AND_INTEREST', 1),
    ('01', 'EQUAL_PRINCIPAL', 2),
    ('02', 'BULK', 1),
    ('03', 'EQUAL_PRINCIPAL_AND_INTEREST', 1),
    ('03', 'EQUAL_PRINCIPAL', 2),
    ('03', 'BULK', 3);

-- 주택담보대출(01): 10/20/30/40년 / 그 외 상품(02, 03): 1/2년
INSERT INTO product_loan_term_rule (product_code, loan_term_years, sort_order) VALUES
    ('01', 10, 1),
    ('01', 20, 2),
    ('01', 30, 3),
    ('01', 40, 4),
    ('02', 1, 1),
    ('02', 2, 2),
    ('03', 1, 1),
    ('03', 2, 2);
