package com.example.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoanScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanScheduleServiceApplication.class, args);
    }
}
