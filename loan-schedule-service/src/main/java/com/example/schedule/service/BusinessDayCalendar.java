package com.example.schedule.service;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * 영업일 계산 -- 토/일요일과 loan-code-service의 공휴일 테이블(holiday)을 모두 제외한다.
 * loan-code-service가 응답하지 않으면 HolidayClient가 빈 Set을 반환하므로 주말만으로 판단한다.
 */
@Component
class BusinessDayCalendar {

    private final HolidayClient holidayClient;

    BusinessDayCalendar(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    LocalDate plusBusinessDays(LocalDate date, int businessDays) {
        Set<LocalDate> holidays = holidayClient.getHolidays();
        LocalDate result = date;
        int added = 0;
        while (added < businessDays) {
            result = result.plusDays(1);
            if (isBusinessDay(result, holidays)) {
                added++;
            }
        }
        return result;
    }

    private boolean isBusinessDay(LocalDate date, Set<LocalDate> holidays) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && !holidays.contains(date);
    }
}
