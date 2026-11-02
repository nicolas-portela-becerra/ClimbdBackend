package com.climbingapp.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.climbingapp")
public class ClimbingAppApplication {

    public static void main(String[] args) {
        // All LocalDateTime values in the app and the database are UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ClimbingAppApplication.class, args);
    }
}
