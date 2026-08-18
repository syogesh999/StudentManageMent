package com.example;

import com.example.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Extensive Integration test suite verifying Spring Boot context startup, HelloController,
 * static frontend assets (index.html, style.css, app.js), and all Student CRUD REST API endpoints.
 * 
 * Uses MockMvc to test full Spring MVC DispatcherServlet handling without starting a physical HTTP server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("SpringBoot Practice Integration & Controller Test Suite")
class SpringBootPracticeApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Context & Static Web Resource Tests")
    class StaticResourceTests {

        @Test
        @DisplayName("Spring ApplicationContext loads successfully")
        void contextLoads() {
            // Verifies Spring ApplicationContext starts cleanly
        }

        @Test
        @DisplayName("GET / should return hello welcome message from HelloController")
        void testHelloRootEndpoint() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, Spring Boot!"));
        }

        @Test
        @DisplayName("GET /index.html should serve static HTML UI with TEXT_HTML content type")
        void testStaticIndexHtml() throws Exception {
            mockMvc.perform(get("/index.html"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_HTML));
        }

        @Test
        @DisplayName("GET /style.css should serve static CSS stylesheet")
        void testStaticStyleCss() throws Exception {
            mockMvc.perform(get("/style.css"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /app.js should serve static JavaScript file")
        void testStaticAppJs() throws Exception {
            mockMvc.perform(get("/app.js"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/students Read API Tests")
    class GetStudentApiTests {

        @Test
        @DisplayName("GET /api/students should return 200 OK with all 3 initial students")
        void testGetAllStudents() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Peter Parker")))
                    .andExpect(jsonPath("$[1].name", is("Tony Stark")))
                    .andExpect(jsonPath("$[2].name", is("Steve Rogers")));
        }

        @Test
        @DisplayName("GET /api/students/ should handle trailing slash route identically")
        void testGetAllStudentsTrailingSlash() throws Exception {
            mockMvc.perform(get("/api/students/"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("GET /api/students/1 should return student #1 details")
        void testGetStudentByIdFound() throws Exception {
            mockMvc.perform(get("/api/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Peter Parker")))
                    .andExpect(jsonPath("$.email", is("peter@example.com")))
                    .andExpect(jsonPath("$.course", is("Computer Science")))
                    .andExpect(jsonPath("$.age", is(21)));
        }

        @Test
        @DisplayName("GET /api/students/2 should return student #2 details")
        void testGetStudentByIdTwo() throws Exception {
            mockMvc.perform(get("/api/students/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.name", is("Tony Stark")))
                    .andExpect(jsonPath("$.email", is("tony@example.com")));
        }

        @Test
        @DisplayName("GET /api/students/999 should return 404 Not Found")
        void testGetStudentByIdNotFound() throws Exception {
            mockMvc.perform(get("/api/students/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("GET /api/students/-1 should return 404 Not Found for negative ID")
        void testGetStudentByIdNegative() throws Exception {
            mockMvc.perform(get("/api/students/-1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("GET /api/students/invalid-type should return 400 Bad Request due to type mismatch")
        void testGetStudentByIdInvalidType() throws Exception {
            mockMvc.perform(get("/api/students/invalid-id-type"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/students Create API Tests")
    class PostStudentApiTests {

        @Test
        @DisplayName("POST /api/students should create and return new student with 201 Created")
        void testCreateStudentSuccess() throws Exception {
            Student newStudent = new Student("Bruce Wayne", "bruce@example.com", "Computer Science", 22);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newStudent)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(4)))
                    .andExpect(jsonPath("$.name", is("Bruce Wayne")))
                    .andExpect(jsonPath("$.email", is("bruce@example.com")))
                    .andExpect(jsonPath("$.course", is("Computer Science")))
                    .andExpect(jsonPath("$.age", is(22)));

            // Verify total count is now 4
            mockMvc.perform(get("/api/students"))
                    .andExpect(jsonPath("$", hasSize(4)));
        }

        @Test
        @DisplayName("POST /api/students/ should handle trailing slash route")
        void testCreateStudentTrailingSlash() throws Exception {
            Student newStudent = new Student("Clark Kent", "clark@example.com", "Journalism", 25);

            mockMvc.perform(post("/api/students/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newStudent)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(4)))
                    .andExpect(jsonPath("$.name", is("Clark Kent")));
        }

        @Test
        @DisplayName("POST /api/students with special characters and boundary values")
        void testCreateStudentSpecialCharsAndBoundaryAge() throws Exception {
            Student special = new Student("Renée O'Connor", "renee.o'connor@example.org", "C++ & AI", 100);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(special)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Renée O'Connor")))
                    .andExpect(jsonPath("$.course", is("C++ & AI")))
                    .andExpect(jsonPath("$.age", is(100)));
        }

        @Test
        @DisplayName("POST /api/students with malformed JSON syntax should return 400 Bad Request")
        void testCreateStudentInvalidJsonSyntax() throws Exception {
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid_json_syntax: }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/students with empty body should return 400 Bad Request")
        void testCreateStudentEmptyBody() throws Exception {
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id} Update API Tests")
    class PutStudentApiTests {

        @Test
        @DisplayName("PUT /api/students/1 should update existing student record")
        void testUpdateStudentSuccess() throws Exception {
            Student updateData = new Student("Peter Parker Updated", "peter.updated@example.com", "Data Science", 22);

            mockMvc.perform(put("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Peter Parker Updated")))
                    .andExpect(jsonPath("$.email", is("peter.updated@example.com")))
                    .andExpect(jsonPath("$.course", is("Data Science")))
                    .andExpect(jsonPath("$.age", is(22)));

            // Verify persistence
            mockMvc.perform(get("/api/students/1"))
                    .andExpect(jsonPath("$.name", is("Peter Parker Updated")));
        }

        @Test
        @DisplayName("PUT /api/students/999 should return 404 Not Found")
        void testUpdateStudentNotFound() throws Exception {
            Student updateData = new Student("Ghost Student", "ghost@example.com", "None", 99);

            mockMvc.perform(put("/api/students/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("PUT /api/students/-1 should return 404 Not Found for negative ID")
        void testUpdateStudentNegativeId() throws Exception {
            Student updateData = new Student("Ghost Student", "ghost@example.com", "None", 99);

            mockMvc.perform(put("/api/students/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("PUT /api/students/invalid-type should return 400 Bad Request")
        void testUpdateStudentInvalidType() throws Exception {
            Student updateData = new Student("Ghost Student", "ghost@example.com", "None", 99);

            mockMvc.perform(put("/api/students/not-an-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /api/students/1 with malformed JSON should return 400 Bad Request")
        void testUpdateStudentMalformedJson() throws Exception {
            mockMvc.perform(put("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ bad_json: "))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id} Delete API Tests")
    class DeleteStudentApiTests {

        @Test
        @DisplayName("DELETE /api/students/2 should delete student and return 204 No Content")
        void testDeleteStudentSuccess() throws Exception {
            mockMvc.perform(delete("/api/students/2"))
                    .andExpect(status().isNoContent());

            // Verify it no longer exists
            mockMvc.perform(get("/api/students/2"))
                    .andExpect(status().isNotFound());

            // Verify count decreased to 2
            mockMvc.perform(get("/api/students"))
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Subsequent DELETE on already deleted ID should return 404 Not Found")
        void testDeleteStudentSubsequentReturnsNotFound() throws Exception {
            // First delete succeeds
            mockMvc.perform(delete("/api/students/2"))
                    .andExpect(status().isNoContent());

            // Second delete returns 404
            mockMvc.perform(delete("/api/students/2"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("DELETE /api/students/999 should return 404 Not Found")
        void testDeleteStudentNotFound() throws Exception {
            mockMvc.perform(delete("/api/students/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("DELETE /api/students/-1 should return 404 Not Found")
        void testDeleteStudentNegativeId() throws Exception {
            mockMvc.perform(delete("/api/students/-1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Student not found")));
        }

        @Test
        @DisplayName("DELETE /api/students/invalid-type should return 400 Bad Request")
        void testDeleteStudentInvalidType() throws Exception {
            mockMvc.perform(delete("/api/students/abc-invalid"))
                    .andExpect(status().isBadRequest());
        }
    }
}
