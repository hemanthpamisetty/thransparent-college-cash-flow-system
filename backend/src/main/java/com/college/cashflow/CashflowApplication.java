package com.college.cashflow;

import com.college.cashflow.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CashflowApplication {

    static {
        // Automatically load .env properties if running under tests or context initialization
        EnvLoader.load();
    }

    public static void main(String[] args) {
        EnvLoader.loadAndValidate();
        SpringApplication.run(CashflowApplication.class, args);
    }
}
