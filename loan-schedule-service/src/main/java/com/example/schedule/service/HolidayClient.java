package com.example.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * loan-code-service가 소유한 공휴일 테이블을 조회한다. loan-code-service가 응답하지 않으면
 * (배포 미완료, 장애 등) 빈 Set을 반환해 BusinessDayCalendar가 주말만으로 영업일을 판단하도록
 * 폴백한다 -- 프론트엔드가 loan-code-service 장애 시 하드코딩 값으로 폴백하는 것과 같은 방식.
 */
@Component
class HolidayClient {

    private static final Logger log = LoggerFactory.getLogger(HolidayClient.class);

    private final RestClient restClient;

    HolidayClient(@Value("${app.code-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    Set<LocalDate> getHolidays() {
        try {
            List<HolidayResponse> holidays = restClient.get()
                    .uri("/api/v1/codes/holidays")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<HolidayResponse>>() {
                    });
            return holidays == null
                    ? Set.of()
                    : holidays.stream().map(HolidayResponse::holidayDt).collect(Collectors.toSet());
        } catch (RestClientException e) {
            log.warn("공휴일 조회 실패 -- loan-code-service 응답 없음, 주말만 영업일 판단에 반영합니다.", e);
            return Set.of();
        }
    }

    record HolidayResponse(LocalDate holidayDt, String holidayName) {
    }
}
