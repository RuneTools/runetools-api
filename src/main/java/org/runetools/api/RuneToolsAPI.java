package org.runetools.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RuneToolsAPI {
    public static void main(String[] args) {
        SpringApplication.run(RuneToolsAPI.class, args);
    }
}
