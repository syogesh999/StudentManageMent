package com.example.controller;

import com.example.model.Student;
import com.example.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST Controller HTTP Endpoint Tests using MockMvc.
 * Tests HTTP status codes, routing, serialization/deserialization, and error responses.
 */
@WebMvcTest(StudentController.class)
@DisplayName("StudentController REST Endpoint MockMvc Tests")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private Student sampleStudent1;
    private Student sampleStudent2;

    @BeforeEach
    void setUp() {
        sampleStudent1 = new Student(1L, "Peter Parker", "peter@example.com", "Computer Science", 21);
        sampleStudent2 = new Student(2L, "Tony Stark", "tony@example.com", "Electrical Engineering", 35);
    }

    @Nested
    @DisplayName("GET /api/students - Read All")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return 200 OK with list of students when records exist")
        void shouldReturnAllStudents() throws Exception {
            when(studentService.getAllStudents()).thenReturn(Arrays.asList(sampleStudent1, sampleStudent2));

            mockMvc.perform(get("/api/students")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Peter Parker")))
                    .andExpect(jsonPath("$[0].email", is("peter@example.com")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Tony Stark")));

            verify(studentService, times(1)).getAllStudents();
        }

        @Test
        @DisplayName("Should return 200 OK with empty array when no students exist")
        void shouldReturnEmptyArrayWhenNoStudents() throws Exception {
            when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/students")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(studentService, times(1)).getAllStudents();
        }

        @Test
        @DisplayName("Should support trailing slash route /api/students/")
        void shouldSupportTrailingSlash() throws Exception {
            when(studentService.getAllStudents()).thenReturn(Collections.singletonList(sampleStudent1));

            mockMvc.perform(get("/api/students/")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Peter Parker")));

            verify(studentService, times(1)).getAllStudents();
        }
    }

    @Nested
    @DisplayName("GET /api/students/{id} - Read By ID")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return 200 OK and student object when ID is found")
        void shouldReturnStudentWhenFound() throws Exception {
            when(studentService.getStudentById(1L)).thenReturn(Optional.of(sampleStudent1));

            mockMvc.perform(get("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Peter Parker")))
                    .andExpect(jsonPath("$.email", is("peter@example.com")))
                    .andExpect(jsonPath("$.course", is("Computer Science")))
                    .andExpect(jsonPath("$.age", is(21)));

            verify(studentService, times(1)).getStudentById(1L);
        }

        @Test
        @DisplayName("Should return 404 Not Found with error message when student does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            when(studentService.getStudentById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/students/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", containsString("Student not found with ID: 999")));

            verify(studentService, times(1)).getStudentById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/students - Create Student")
    class CreateStudentTests {

        @Test
        @DisplayName("Should return 201 Created and JSON body on successful student creation")
        void shouldCreateStudentSuccessfully() throws Exception {
            Student inputStudent = new Student("Bruce Banner", "bruce@example.com", "Nuclear Physics", 40);
            Student savedStudent = new Student(3L, "Bruce Banner", "bruce@example.com", "Nuclear Physics", 40);

            when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputStudent)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.name", is("Bruce Banner")))
                    .andExpect(jsonPath("$.email", is("bruce@example.com")))
                    .andExpect(jsonPath("$.course", is("Nuclear Physics")))
                    .andExpect(jsonPath("$.age", is(40)));

            verify(studentService, times(1)).createStudent(any(Student.class));
        }

        @Test
        @DisplayName("Should support trailing slash on POST /api/students/")
        void shouldSupportTrailingSlashOnCreate() throws Exception {
            Student inputStudent = new Student("Bruce Banner", "bruce@example.com", "Nuclear Physics", 40);
            Student savedStudent = new Student(3L, "Bruce Banner", "bruce@example.com", "Nuclear Physics", 40);

            when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);

            mockMvc.perform(post("/api/students/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputStudent)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(3)));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is empty/malformed")
        void shouldReturn400OnEmptyBody() throws Exception {
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id} - Update Student")
    class UpdateStudentTests {

        @Test
        @DisplayName("Should return 200 OK and updated JSON when update is successful")
        void shouldUpdateStudentSuccessfully() throws Exception {
            Student updateData = new Student("Peter Parker", "peter.updated@example.com", "Computer Science", 22);
            Student updatedResult = new Student(1L, "Peter Parker", "peter.updated@example.com", "Computer Science", 22);

            when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(Optional.of(updatedResult));

            mockMvc.perform(put("/api/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Peter Parker")))
                    .andExpect(jsonPath("$.email", is("peter.updated@example.com")))
                    .andExpect(jsonPath("$.age", is(22)));

            verify(studentService, times(1)).updateStudent(eq(1L), any(Student.class));
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent student")
        void shouldReturn404WhenUpdatingNonExistentStudent() throws Exception {
            Student updateData = new Student("Ghost", "ghost@example.com", "Unknown", 99);

            when(studentService.updateStudent(eq(999L), any(Student.class))).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/students/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", containsString("Student not found with ID: 999")));

            verify(studentService, times(1)).updateStudent(eq(999L), any(Student.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id} - Delete Student")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should return 204 No Content when delete is successful")
        void shouldDeleteStudentSuccessfully() throws Exception {
            when(studentService.deleteStudent(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/students/1"))
                    .andExpect(status().isNoContent());

            verify(studentService, times(1)).deleteStudent(1L);
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent student")
        void shouldReturn404WhenDeletingNonExistentStudent() throws Exception {
            when(studentService.deleteStudent(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/students/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", containsString("Student not found with ID: 999")));

            verify(studentService, times(1)).deleteStudent(999L);
        }
    }
}
