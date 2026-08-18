package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot application.
 * 
 * The @SpringBootApplication annotation enables:
 * - @ComponentScan: Scans for Spring components in this package and sub-packages
 * - @Configuration: Declares this as a configuration class
 * - @EnableAutoConfiguration: Enables Spring Boot auto-configuration
 * 
 * When you run this application:
 * 1. Spring creates an ApplicationContext
 * 2. Auto-configures the embedded Tomcat server
 * 3. Starts listening on port 8080 by default
 * 4. Scans for @Controller, @Service, @Repository classes
 */
@SpringBootApplication
public class SpringBootPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootPracticeApplication.class, args);
    }
}
