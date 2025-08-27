package com.itjima_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ItjimaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItjimaApplication.class, args);
    }

}
