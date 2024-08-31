package com.ojo.passkeydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PasskeydemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PasskeydemoApplication.class, args);
    }

}
