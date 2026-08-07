package com.example.code.dto;

import java.time.LocalDate;

public record HolidayResponse(LocalDate holidayDt, String holidayName) {
}
