package com.example.ledger.service;

import com.example.ledger.domain.LonAcctBase;
import com.example.ledger.domain.LonAcctBaseId;
import com.example.ledger.dto.LedgerAccountCreateRequest;
import com.example.ledger.dto.LedgerAccountResponse;
import com.example.ledger.dto.LedgerAccountUpdateRequest;
import com.example.ledger.repository.LonAcctBaseRepository;
import com.example.ledger.repository.SeqCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LedgerAccountService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    // 신규일자로부터 이자납입일까지 이 일수 미만이면 첫 이자납입을 익월로 이월한다(최소 유예기간).
    private static final int MIN_GRACE_DAYS = 15;
    // seq_counter.seq_cd -- 계좌번호 채번룰. 구분값(seq_div_cd)은 과목코드(01/02/03).
    private static final String SEQ_CD_ACCT_NO = "LONSEQ";

    private final LonAcctBaseRepository lonAcctBaseRepository;
    private final SeqCounterRepository seqCounterRepository;

    public LedgerAccountService(LonAcctBaseRepository lonAcctBaseRepository, SeqCounterRepository seqCounterRepository) {
        this.lonAcctBaseRepository = lonAcctBaseRepository;
        this.seqCounterRepository = seqCounterRepository;
    }

    public List<LedgerAccountResponse> search(String keyword) {
        List<LonAcctBase> accounts = StringUtils.hasText(keyword)
                ? lonAcctBaseRepository.findByAcctNoContainingOrCustNoContainingOrderByAcctNoAscAcctSeqNoAsc(keyword, keyword)
                : lonAcctBaseRepository.findAllByOrderByAcctNoAscAcctSeqNoAsc();

        return accounts.stream().map(LedgerAccountResponse::from).toList();
    }

    // 계좌번호 검색 팝업(이자계산/원장조회 화면 공용) 전용 -- 고객번호/고객명/계좌상태로 AND 검색.
    public List<LedgerAccountResponse> searchForAccountPicker(String custNo, String custName, String acctStatCd) {
        return lonAcctBaseRepository.searchForAccountPicker(custNo, custName, acctStatCd)
                .stream().map(LedgerAccountResponse::from).toList();
    }

    public LedgerAccountResponse getOne(String acctNo, Integer acctSeqNo) {
        return LedgerAccountResponse.from(findOrThrow(acctNo, acctSeqNo));
    }

    // 신규 원장 개설. 계좌번호는 (신규일자+과목코드) 채번 규칙으로, 계좌상태/잔액/납입일자 등은
    // 아래 규칙으로 서버가 계산해서 채운다 -- 화면은 실제 입력이 필요한 항목만 보낸다.
    // 화면에서 이미 필수값 검증을 하지만, API를 직접 호출하는 경우(curl 등)를 대비해 여기서도
    // 검증한다 -- 검증 없이 바로 파싱하면 필수값 누락 시 NPE로 500(빈 메시지)이 나서 원인을 알 수 없었다.
    public LedgerAccountResponse register(LedgerAccountCreateRequest request) {
        validateCreateRequest(request);

        LocalDate newDate = parseDateOrThrow(request.newDt(), "신규일자");
        LocalDate matDate = parseDateOrThrow(request.matDt(), "만기일자");
        if (!matDate.isAfter(newDate)) {
            throw new IllegalArgumentException("만기일자는 신규일자보다 이후 날짜여야 합니다.");
        }
        int monthlyPayDay = parseMonthlyPayDayOrThrow(request.monthlyIntPayDay());

        LocalDate nextPayDate = computeNextPayDate(newDate, monthlyPayDay);
        LocalDate deadlineLossDate = nextPayDate.plusMonths(1);
        String nextPayDt = nextPayDate.format(YYYYMMDD);

        String acctNo = generateAcctNo(request.newDt(), request.itemCd());

        LonAcctBase account = new LonAcctBase(
                acctNo, 1, request.custNo(), request.custName(),
                "01", request.itemCd(), null, null,
                request.loanLimitAmt(), request.loanLimitAmt(), request.newDt(), request.matDt(),
                nextPayDt, nextPayDt, request.newDt(), request.newDt(),
                deadlineLossDate.format(YYYYMMDD), request.monthlyIntPayDay(), request.baseRate(), request.addRate(),
                request.applyRate(), request.earlyRepayFeeRate(), request.repayMethodCd(),
                request.rateChangeTypeCd(), request.rateChangeCycle(), null);

        return LedgerAccountResponse.from(lonAcctBaseRepository.save(account));
    }

    // 화면이 보내야 하는 13개 입력 필드 전부 필수 -- 화면의 REQUIRED_FIELD_MESSAGES와 문구를 맞춘다
    // (조사 은/는은 필드명 받침에 따라 다르므로 라벨 조합이 아니라 필드별 완성 문장을 그대로 쓴다).
    private void validateCreateRequest(LedgerAccountCreateRequest request) {
        requireText(request.custNo(), "고객번호는 필수입력사항입니다.");
        requireText(request.custName(), "고객명은 필수입력사항입니다.");
        requireText(request.itemCd(), "과목은 필수입력사항입니다.");
        requireNonNull(request.loanLimitAmt(), "대출한도는 필수입력사항입니다.");
        requireText(request.newDt(), "신규일자는 필수입력사항입니다.");
        requireText(request.matDt(), "만기일자는 필수입력사항입니다.");
        requireText(request.monthlyIntPayDay(), "매월이자납입일은 필수입력사항입니다.");
        requireNonNull(request.baseRate(), "기준금리는 필수입력사항입니다.");
        requireNonNull(request.addRate(), "가산금리는 필수입력사항입니다.");
        requireNonNull(request.earlyRepayFeeRate(), "조기상환수수료율은 필수입력사항입니다.");
        requireText(request.repayMethodCd(), "상환방식은 필수입력사항입니다.");
        requireText(request.rateChangeTypeCd(), "금리유형은 필수입력사항입니다.");
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static LocalDate parseDateOrThrow(String value, String label) {
        try {
            return LocalDate.parse(value, YYYYMMDD);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(label + " 형식이 올바르지 않습니다: " + value);
        }
    }

    private static int parseMonthlyPayDayOrThrow(String value) {
        int day;
        try {
            day = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("매월이자납입일은 숫자로 입력해주세요.");
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("매월이자납입일은 1~31 사이의 값이어야 합니다.");
        }
        return day;
    }

    // 채번룰: {신규일자:YYYYMMDD}{과목코드:2자리}{일련번호:4자리}, 일련번호는 seq_counter의
    // (LONSEQ, 과목코드) 카운터에서 원자적으로 발급받는다 -- 과목코드별로 계속 누적되고 날짜별로
    // 리셋되지 않지만, 계좌번호 자체에 신규일자가 이미 포함돼 있어 유일성은 그대로 보장된다.
    // 기존 시드 데이터(예: 20210610010001)와 동일한 문자열 형식.
    private String generateAcctNo(String newDt, String itemCd) {
        int nextSeq = seqCounterRepository.incrementAndGetNextSeq(SEQ_CD_ACCT_NO, itemCd);
        if (nextSeq > 9999) {
            throw new IllegalArgumentException("해당 과목의 계좌 채번 범위(9999건)를 초과했습니다: " + itemCd);
        }
        return newDt + itemCd + String.format("%04d", nextSeq);
    }

    // 신규일자 기준 이 달의 매월이자납입일 후보가 15일 미만의 유예기간만 남는다면(이미 지났거나
    // 너무 임박) 익월로 이월한다 -- 예: 8/12 신규, 납입일 15일 → 8/15는 3일 뒤라 이월, 9/15로 설정.
    private static LocalDate computeNextPayDate(LocalDate newDate, int monthlyPayDay) {
        LocalDate candidate = clampToDay(YearMonth.from(newDate), monthlyPayDay);
        if (ChronoUnit.DAYS.between(newDate, candidate) < MIN_GRACE_DAYS) {
            candidate = clampToDay(YearMonth.from(newDate).plusMonths(1), monthlyPayDay);
        }
        return candidate;
    }

    private static LocalDate clampToDay(YearMonth yearMonth, int day) {
        return yearMonth.atDay(Math.min(day, yearMonth.lengthOfMonth()));
    }

    public LedgerAccountResponse update(String acctNo, Integer acctSeqNo, LedgerAccountUpdateRequest request) {
        LonAcctBase account = findOrThrow(acctNo, acctSeqNo);

        account.setCustNo(request.custNo());
        account.setCustName(request.custName());
        account.setAcctStatCd(request.acctStatCd());
        account.setItemCd(request.itemCd());
        account.setApplyNo(request.applyNo());
        account.setApprovalNo(request.approvalNo());
        account.setLoanLimitAmt(request.loanLimitAmt());
        account.setLoanBalAmt(request.loanBalAmt());
        account.setNewDt(request.newDt());
        account.setMatDt(request.matDt());
        account.setNextIntPayDt(request.nextIntPayDt());
        account.setNextRepayDt(request.nextRepayDt());
        account.setLastIntPayDt(request.lastIntPayDt());
        account.setLastRepayDt(request.lastRepayDt());
        account.setDeadlineLossDt(request.deadlineLossDt());
        account.setMonthlyIntPayDay(request.monthlyIntPayDay());
        account.setBaseRate(request.baseRate());
        account.setAddRate(request.addRate());
        account.setApplyRate(request.applyRate());
        account.setEarlyRepayFeeRate(request.earlyRepayFeeRate());
        account.setRepayMethodCd(request.repayMethodCd());
        account.setRateChangeTypeCd(request.rateChangeTypeCd());
        account.setRateChangeCycle(request.rateChangeCycle());
        account.setVirtualAcctNo(request.virtualAcctNo());

        return LedgerAccountResponse.from(lonAcctBaseRepository.save(account));
    }

    public void delete(String acctNo, Integer acctSeqNo) {
        LonAcctBaseId id = new LonAcctBaseId(acctNo, acctSeqNo);
        if (!lonAcctBaseRepository.existsById(id)) {
            throw new NoSuchElementException("계좌를 찾을 수 없습니다: " + acctNo + "-" + acctSeqNo);
        }
        lonAcctBaseRepository.deleteById(id);
    }

    private LonAcctBase findOrThrow(String acctNo, Integer acctSeqNo) {
        return lonAcctBaseRepository.findById(new LonAcctBaseId(acctNo, acctSeqNo))
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다: " + acctNo + "-" + acctSeqNo));
    }
}
