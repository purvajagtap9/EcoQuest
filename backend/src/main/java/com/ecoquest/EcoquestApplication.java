package com.ecoquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the EcoQuest India backend.
 * Boots a Spring Boot app backed by MongoDB (see application.yml for connection config).
 */
@SpringBootApplication
public class EcoquestApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoquestApplication.class, args);
    }
}
