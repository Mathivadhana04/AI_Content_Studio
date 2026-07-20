package com.aicontentstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiContentStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiContentStudioApplication.class, args);
    }
}
