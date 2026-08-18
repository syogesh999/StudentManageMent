package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Student Management System.
 *
 * @SpringBootApplication is a shortcut for:
 *   - @Configuration       → marks this class as a source of Spring Beans
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure based on classpath
 *   - @ComponentScan       → scans com.example and sub-packages for @Component, @Service, @RestController, etc.
 */
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
