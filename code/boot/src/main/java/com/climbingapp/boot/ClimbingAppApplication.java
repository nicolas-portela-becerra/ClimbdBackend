package com.climbingapp.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.climbingapp")
@EnableScheduling
public class ClimbingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClimbingAppApplication.class, args);
    }
}
