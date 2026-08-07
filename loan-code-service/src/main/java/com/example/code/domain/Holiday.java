package com.example.code.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "holiday")
public class Holiday {

    @Id
    private LocalDate holidayDt;

    private String holidayName;

    protected Holiday() {
    }

    public LocalDate getHolidayDt() {
        return holidayDt;
    }

    public String getHolidayName() {
        return holidayName;
    }
}
