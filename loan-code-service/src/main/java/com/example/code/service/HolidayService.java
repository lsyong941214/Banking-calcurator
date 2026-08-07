package com.example.code.service;

import com.example.code.dto.HolidayResponse;
import com.example.code.repository.HolidayRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public List<HolidayResponse> getHolidays() {
        return holidayRepository.findAll().stream()
                .map(h -> new HolidayResponse(h.getHolidayDt(), h.getHolidayName()))
                .toList();
    }
}
