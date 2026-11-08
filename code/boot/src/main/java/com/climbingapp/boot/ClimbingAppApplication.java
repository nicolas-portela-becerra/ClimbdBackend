package com.climbingapp.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.climbingapp")
public class ClimbingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClimbingAppApplication.class, args);
    }
}
