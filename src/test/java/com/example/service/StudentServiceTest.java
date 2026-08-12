package com.example.service;

import com.example.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensive Unit test suite for StudentService business logic isolated from Web/HTTP infrastructure.
 * Tests initial state, CRUD operations, concurrency behavior, boundary ages, negative IDs, and list immutability.
 */
@DisplayName("StudentService Business Logic Unit Tests")
class StudentServiceTest {

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
    }

    @Nested
    @DisplayName("Initialization & Read Tests")
    class ReadTests {

        @Test
        @DisplayName("Should initialize with 3 pre-populated sample students")
        void testInitialStudents() {
            List<Student> students = studentService.getAllStudents();
            assertEquals(3, students.size(), "Initial student list should contain 3 items");
        }

        @Test
        @DisplayName("Should return defensive copy of student list that prevents internal state mutation")
        void testGetAllStudentsImmutability() {
            List<Student> students = studentService.getAllStudents();
            students.clear(); // Mutate returned list

            assertEquals(3, studentService.getAllStudents().size(), "Clearing external list should not affect internal map");
        }

        @Test
        @DisplayName("Should retrieve student by valid existing ID (1L, 2L, 3L)")
        void testGetStudentByIdFound() {
            Optional<Student> student1 = studentService.getStudentById(1L);
            assertTrue(student1.isPresent(), "Student with ID 1 should exist");
            assertEquals("Peter Parker", student1.get().getName());
            assertEquals("peter@example.com", student1.get().getEmail());

            Optional<Student> student2 = studentService.getStudentById(2L);
            assertTrue(student2.isPresent(), "Student with ID 2 should exist");
            assertEquals("Tony Stark", student2.get().getName());

            Optional<Student> student3 = studentService.getStudentById(3L);
            assertTrue(student3.isPresent(), "Student with ID 3 should exist");
            assertEquals("Steve Rogers", student3.get().getName());
        }

        @Test
        @DisplayName("Should return empty Optional when student ID does not exist")
        void testGetStudentByIdNotFound() {
            Optional<Student> student = studentService.getStudentById(999L);
            assertFalse(student.isPresent(), "Student with ID 999 should not exist");
        }

        @Test
        @DisplayName("Should return empty Optional when passing negative or zero ID")
        void testGetStudentByIdNegativeOrZero() {
            assertFalse(studentService.getStudentById(-1L).isPresent(), "Negative ID should not be found");
            assertFalse(studentService.getStudentById(0L).isPresent(), "Zero ID should not be found");
        }

        @Test
        @DisplayName("Should handle null ID parameter gracefully")
        void testGetStudentByIdNull() {
            Optional<Student> student = studentService.getStudentById(null);
            assertFalse(student.isPresent(), "Null ID should return empty Optional");
        }
    }

    @Nested
    @DisplayName("Creation (Create) Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create a new student with auto-incremented ID starting from 4")
        void testCreateStudent() {
            Student newStudent = new Student("Natasha Romanoff", "natasha@example.com", "Tactical Operations", 29);
            Student created = studentService.createStudent(newStudent);

            assertNotNull(created.getId(), "Generated student ID should not be null");
            assertEquals(4L, created.getId(), "First created student ID should be 4");
            assertEquals("Natasha Romanoff", created.getName());
            assertEquals(4, studentService.getAllStudents().size(), "Total students count should now be 4");
        }

        @Test
        @DisplayName("Should sequentially increment IDs for multiple creations (4L, 5L, 6L)")
        void testMultipleCreationsSequentialIds() {
            Student s1 = studentService.createStudent(new Student("Student One", "one@example.com", "CS", 20));
            Student s2 = studentService.createStudent(new Student("Student Two", "two@example.com", "EE", 21));
            Student s3 = studentService.createStudent(new Student("Student Three", "three@example.com", "ME", 22));

            assertEquals(4L, s1.getId());
            assertEquals(5L, s2.getId());
            assertEquals(6L, s3.getId());
            assertEquals(6, studentService.getAllStudents().size());
        }

        @Test
        @DisplayName("Should create student with special characters and boundary ages")
        void testCreateStudentSpecialCharsAndBoundaryAge() {
            Student special = new Student("François D'Amboise", "francois@example.fr", "Fine Arts & Literature", 100);
            Student created = studentService.createStudent(special);

            assertEquals(4L, created.getId());
            assertEquals("François D'Amboise", created.getName());
            assertEquals(100, created.getAge());
        }
    }

    @Nested
    @DisplayName("Update (PUT) Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing student successfully")
        void testUpdateStudentSuccess() {
            Student updateData = new Student("Peter Parker Updated", "peter.new@example.com", "AI Science", 22);
            Optional<Student> updated = studentService.updateStudent(1L, updateData);

            assertTrue(updated.isPresent(), "Update operation should return updated student");
            assertEquals(1L, updated.get().getId(), "ID should remain 1");
            assertEquals("Peter Parker Updated", updated.get().getName());
            assertEquals("peter.new@example.com", updated.get().getEmail());
            assertEquals("AI Science", updated.get().getCourse());
            assertEquals(22, updated.get().getAge());
        }

        @Test
        @DisplayName("Should enforce path variable ID over payload ID during update")
        void testUpdateStudentEnforcesPathId() {
            Student updateDataWithDifferentId = new Student(999L, "Mismatched ID Name", "test@example.com", "Math", 25);
            Optional<Student> updated = studentService.updateStudent(1L, updateDataWithDifferentId);

            assertTrue(updated.isPresent());
            assertEquals(1L, updated.get().getId(), "Path variable ID 1L must overwrite payload ID 999L");
        }

        @Test
        @DisplayName("Should return empty Optional when updating non-existent student ID (999L)")
        void testUpdateStudentNotFound() {
            Student updateData = new Student("Ghost", "ghost@example.com", "None", 30);
            Optional<Student> updated = studentService.updateStudent(999L, updateData);

            assertFalse(updated.isPresent(), "Updating ID 999 should return empty Optional");
        }

        @Test
        @DisplayName("Should return empty Optional when updating negative student ID")
        void testUpdateStudentNegativeId() {
            Student updateData = new Student("Ghost", "ghost@example.com", "None", 30);
            Optional<Student> updated = studentService.updateStudent(-5L, updateData);

            assertFalse(updated.isPresent(), "Updating negative ID should return empty Optional");
        }
    }

    @Nested
    @DisplayName("Deletion (DELETE) Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete existing student successfully")
        void testDeleteStudentSuccess() {
            boolean deleted = studentService.deleteStudent(1L);
            assertTrue(deleted, "Delete should return true for existing student ID 1");
            assertFalse(studentService.getStudentById(1L).isPresent(), "Student 1 should no longer exist");
            assertEquals(2, studentService.getAllStudents().size(), "Total students count should be 2");
        }

        @Test
        @DisplayName("Should enforce idempotency: second delete on same ID returns false")
        void testDeleteStudentIdempotency() {
            assertTrue(studentService.deleteStudent(1L), "First delete should return true");
            assertFalse(studentService.deleteStudent(1L), "Second delete on same ID should return false");
        }

        @Test
        @DisplayName("Should return false when deleting non-existent student ID (999L)")
        void testDeleteStudentNotFound() {
            boolean deleted = studentService.deleteStudent(999L);
            assertFalse(deleted, "Delete should return false for non-existent student ID 999");
            assertEquals(3, studentService.getAllStudents().size(), "Total students count should remain 3");
        }

        @Test
        @DisplayName("Should return false when deleting negative ID (-1L)")
        void testDeleteStudentNegativeId() {
            boolean deleted = studentService.deleteStudent(-1L);
            assertFalse(deleted, "Delete should return false for negative ID -1");
            assertEquals(3, studentService.getAllStudents().size());
        }
    }
}
