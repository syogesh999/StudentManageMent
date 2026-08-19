package com.example.repository;

import com.example.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository Integration Tests using H2 in-memory database.
 * Uses @DataJpaTest which auto-configures JPA, Hibernate, and an embedded
 * datasource. The "test" profile points to H2 via application-test.properties.
 *
 * Each test method runs in a transaction that is rolled back after completion,
 * ensuring test isolation.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudentRepository Integration Tests (H2)")
class StudentRepositoryIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDatabase() {
        studentRepository.deleteAll();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {

        @Test
        @DisplayName("Should save student and generate auto-incremented ID")
        void shouldSaveAndGenerateId() {
            // Arrange
            Student student = new Student("Diana Prince", "diana@example.com", "History", 28);

            // Act
            Student saved = studentRepository.save(student);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(saved.getId() > 0);
            assertEquals("Diana Prince", saved.getName());
            assertEquals("diana@example.com", saved.getEmail());
            assertEquals("History", saved.getCourse());
            assertEquals(28, saved.getAge());
        }

        @Test
        @DisplayName("Should save and retrieve student with all fields intact")
        void shouldSaveAndRetrieveStudent() {
            // Arrange
            Student student = new Student("Clark Kent", "clark@example.com", "Journalism", 30);
            Student saved = studentRepository.save(student);

            // Act
            Optional<Student> found = studentRepository.findById(saved.getId());

            // Assert
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals("Clark Kent", found.get().getName());
            assertEquals("clark@example.com", found.get().getEmail());
            assertEquals("Journalism", found.get().getCourse());
            assertEquals(30, found.get().getAge());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindTests {

        @Test
        @DisplayName("Should find all students when multiple records exist")
        void shouldFindAllStudents() {
            // Arrange
            studentRepository.save(new Student("Alice", "alice@example.com", "Math", 20));
            studentRepository.save(new Student("Bob", "bob@example.com", "Science", 22));
            studentRepository.save(new Student("Charlie", "charlie@example.com", "English", 19));

            // Act
            List<Student> students = studentRepository.findAll();

            // Assert
            assertEquals(3, students.size());
        }

        @Test
        @DisplayName("Should return empty list when no students exist in database")
        void shouldReturnEmptyListWhenNoStudents() {
            // Act
            List<Student> students = studentRepository.findAll();

            // Assert
            assertNotNull(students);
            assertTrue(students.isEmpty());
        }

        @Test
        @DisplayName("Should return empty Optional for non-existent ID")
        void shouldReturnEmptyOptionalForNonExistentId() {
            // Act
            Optional<Student> result = studentRepository.findById(999L);

            // Assert
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing student fields and persist changes")
        void shouldUpdateExistingStudent() {
            // Arrange
            Student student = studentRepository.save(
                    new Student("Original Name", "original@example.com", "Original Course", 20));
            Long savedId = student.getId();

            // Act — modify and re-save
            student.setName("Updated Name");
            student.setEmail("updated@example.com");
            student.setCourse("Updated Course");
            student.setAge(25);
            studentRepository.save(student);

            // Assert — fetch fresh from DB
            Optional<Student> updated = studentRepository.findById(savedId);
            assertTrue(updated.isPresent());
            assertEquals("Updated Name", updated.get().getName());
            assertEquals("updated@example.com", updated.get().getEmail());
            assertEquals("Updated Course", updated.get().getCourse());
            assertEquals(25, updated.get().getAge());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete student and confirm it no longer exists")
        void shouldDeleteStudent() {
            // Arrange
            Student student = studentRepository.save(
                    new Student("To Delete", "delete@example.com", "Temp", 20));
            Long savedId = student.getId();
            assertTrue(studentRepository.existsById(savedId));

            // Act
            studentRepository.deleteById(savedId);

            // Assert
            assertFalse(studentRepository.existsById(savedId));
            Optional<Student> result = studentRepository.findById(savedId);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Database Constraint Tests")
    class ConstraintTests {

        @Test
        @DisplayName("Should reject duplicate email addresses due to unique constraint")
        void shouldRejectDuplicateEmail() {
            // Arrange
            studentRepository.save(new Student("First Student", "same@example.com", "Math", 20));

            // Act & Assert — second save with same email should fail
            Student duplicate = new Student("Second Student", "same@example.com", "Science", 22);
            assertThrows(DataIntegrityViolationException.class,
                    () -> {
                        studentRepository.save(duplicate);
                        studentRepository.flush(); // Force Hibernate to write to DB immediately
                    });
        }
    }
}
