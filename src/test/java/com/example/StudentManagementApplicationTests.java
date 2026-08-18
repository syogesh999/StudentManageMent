package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test verifying that the Spring Boot ApplicationContext loads successfully.
 */
@SpringBootTest
@DisplayName("Application Context Load Smoke Test")
class StudentManagementApplicationTests {

    @Test
    @DisplayName("Application context should load without exceptions")
    void contextLoads() {
    }
}
