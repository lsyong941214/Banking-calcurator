package com.example.calcurator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Screen only -- no business logic, no DB. The static page it serves calls
 * loan-engine-service (interest calc), loan-schedule-service (repayment
 * schedule preview), and loan-code-service (code tables) directly from the browser.
 */
@SpringBootApplication
public class LoanCalcuratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanCalcuratorApplication.class, args);
    }
}
