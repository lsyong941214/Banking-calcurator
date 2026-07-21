-- Loan MSA reference schema (PostgreSQL)
-- All money/rate fields use NUMERIC to avoid floating-point drift.

-- 1. 대출 기본 정보 테이블
CREATE TABLE loan_contract (
    loan_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    principal_amount NUMERIC(15, 2) NOT NULL, -- 대출 원금
    annual_rate NUMERIC(5, 4) NOT NULL,       -- 연이율 (예: 0.0550 = 5.5%)
    term_months INT NOT NULL,                  -- 대출 기간 (월)
    repayment_type VARCHAR(20) NOT NULL,       -- EQUAL_PRINCIPAL_AND_INTEREST (원리금균등), EQUAL_PRINCIPAL (원금균등), BULK (만기일시)
    start_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 상환 스케줄 테이블
CREATE TABLE repayment_schedule (
    schedule_id BIGSERIAL PRIMARY KEY,
    loan_id VARCHAR(36) REFERENCES loan_contract(loan_id),
    sequence INT NOT NULL,                    -- 회차 (1회차, 2회차...)
    due_date DATE NOT NULL,                   -- 상환 예정일
    principal_payment NUMERIC(15, 2) NOT NULL,-- 상환 원금
    interest_payment NUMERIC(15, 2) NOT NULL, -- 상환 이자
    total_payment NUMERIC(15, 2) NOT NULL,    -- 납입 총액 (원금 + 이자)
    remaining_balance NUMERIC(15, 2) NOT NULL,-- 상환 후 잔액
    status VARCHAR(20) DEFAULT 'PENDING'       -- PENDING, PAID, OVERDUE
);
