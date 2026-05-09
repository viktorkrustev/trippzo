package com.trippzo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrippzoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrippzoApplication.class, args);
    }
}
