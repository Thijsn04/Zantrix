package com.zantrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Zantrix Backend.
 * <p>
 * Bootstraps the Spring Boot application. Zantrix is designed using a modular monolith
 * architecture (Modulith) to keep domains like IAM, Scheduling, PMI, and Terminology
 * cleanly separated while deploying as a single artifact.
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class ZantrixApplication {

    /**
     * The main method that starts the Spring Boot application.
     *
     * @param args Command-line arguments passed during startup.
     */
    public static void main(String[] args) {
        SpringApplication.run(ZantrixApplication.class, args);
    }
}
