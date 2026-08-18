package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test verifying that the Spring Boot ApplicationContext loads successfully.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Application Context Load Smoke Test")
class StudentManagementApplicationTests {

    @Test
    @DisplayName("Application context should load without exceptions")
    void contextLoads() {
    }
}
