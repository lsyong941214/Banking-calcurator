package com.example.schedule.service;

import com.example.schedule.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 계좌의 다음이자납입일자/기한이익상실일자와 조회 시점(referenceDate)만 보고 현재 상태를
 * 정상/연체/기한이익상실 중 하나로 판정하는 공통 모듈. 실제 금액 계산(정상/연체 이자)은
 * NormalInterestCalculator/OverdueInterestCalculator가 각각 맡고, 이 클래스는 "어느 계산을
 * 태울지"만 결정한다.
 */
@Component
class PaymentStatusAnalyzer {

    private final BusinessDayCalendar businessDayCalendar;

    PaymentStatusAnalyzer(BusinessDayCalendar businessDayCalendar) {
        this.businessDayCalendar = businessDayCalendar;
    }

    PaymentStatusResult analyze(LocalDate nextIntPayDt, LocalDate deadlineLossDt, LocalDate referenceDate) {
        LocalDate overdueStartDate = businessDayCalendar.plusBusinessDays(nextIntPayDt, 1);

        if (!referenceDate.isAfter(overdueStartDate)) {
            return new PaymentStatusResult(PaymentStatus.NORMAL, overdueStartDate);
        }
        if (deadlineLossDt != null && referenceDate.isAfter(deadlineLossDt)) {
            return new PaymentStatusResult(PaymentStatus.ACCELERATED, overdueStartDate);
        }
        return new PaymentStatusResult(PaymentStatus.OVERDUE_INSTALLMENT, overdueStartDate);
    }

    record PaymentStatusResult(PaymentStatus status, LocalDate overdueStartDate) {
    }
}
