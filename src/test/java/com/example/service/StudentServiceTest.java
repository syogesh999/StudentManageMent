package com.example.service;

import com.example.model.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Unit Test Suite for StudentService business logic.
 * Follows Arrange -> Act -> Assert pattern with full Mockito isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Business Logic Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student sampleStudent1;
    private Student sampleStudent2;

    @BeforeEach
    void setUp() {
        sampleStudent1 = new Student(1L, "Peter Parker", "peter@example.com", "Computer Science", 21);
        sampleStudent2 = new Student(2L, "Tony Stark", "tony@example.com", "Electrical Engineering", 35);
    }

    @Nested
    @DisplayName("Read Operations (getAllStudents & getStudentById)")
    class ReadTests {

        @Test
        @DisplayName("Should retrieve all students when database has records")
        void testGetAllStudentsWhenRecordsExist() {
            // Arrange
            when(studentRepository.findAll()).thenReturn(Arrays.asList(sampleStudent1, sampleStudent2));

            // Act
            List<Student> students = studentService.getAllStudents();

            // Assert
            assertNotNull(students);
            assertEquals(2, students.size());
            assertEquals("Peter Parker", students.get(0).getName());
            assertEquals("Tony Stark", students.get(1).getName());
            verify(studentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when database has no records")
        void testGetAllStudentsWhenDatabaseIsEmpty() {
            // Arrange
            when(studentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Student> students = studentService.getAllStudents();

            // Assert
            assertNotNull(students);
            assertTrue(students.isEmpty());
            verify(studentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return safe empty list when repository returns null")
        void testGetAllStudentsWhenRepositoryReturnsNull() {
            // Arrange
            when(studentRepository.findAll()).thenReturn(null);

            // Act
            List<Student> students = studentService.getAllStudents();

            // Assert
            assertNotNull(students);
            assertTrue(students.isEmpty());
            verify(studentRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should retrieve student by ID successfully when ID exists")
        void testGetStudentByIdFound() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent1));

            // Act
            Optional<Student> result = studentService.getStudentById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Peter Parker", result.get().getName());
            assertEquals("peter@example.com", result.get().getEmail());
            verify(studentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return empty Optional when student ID does not exist")
        void testGetStudentByIdNotFound() {
            // Arrange
            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Student> result = studentService.getStudentById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(studentRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should return empty Optional when passing null ID without querying repository")
        void testGetStudentByIdWithNullId() {
            // Act
            Optional<Student> result = studentService.getStudentById(null);

            // Assert
            assertFalse(result.isPresent());
            verify(studentRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Create Operations (createStudent)")
    class CreateTests {

        @Test
        @DisplayName("Should create student successfully with auto-generated ID from repository")
        void testCreateStudentSuccess() {
            // Arrange
            Student inputStudent = new Student(null, "Natasha Romanoff", "natasha@example.com", "Tactics", 29);
            Student savedStudent = new Student(4L, "Natasha Romanoff", "natasha@example.com", "Tactics", 29);
            when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

            // Act
            Student result = studentService.createStudent(inputStudent);

            // Assert
            assertNotNull(result);
            assertEquals(4L, result.getId());
            assertEquals("Natasha Romanoff", result.getName());
            assertEquals("natasha@example.com", result.getEmail());
            verify(studentRepository, times(1)).save(inputStudent);
        }

        @Test
        @DisplayName("Should enforce null ID on input to let database assign generated key")
        void testCreateStudentClearsSuppliedId() {
            // Arrange
            Student studentWithId = new Student(999L, "Clint Barton", "clint@example.com", "Archery", 33);
            Student savedStudent = new Student(5L, "Clint Barton", "clint@example.com", "Archery", 33);
            when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

            // Act
            Student result = studentService.createStudent(studentWithId);

            // Assert
            assertEquals(5L, result.getId());
            verify(studentRepository).save(argThat(s -> s.getId() == null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when creating null student")
        void testCreateStudentNullPayload() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> studentService.createStudent(null));
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate database integrity exception on duplicate unique constraint")
        void testCreateStudentDuplicateEmailException() {
            // Arrange
            Student duplicate = new Student("Peter Parker", "peter@example.com", "CS", 21);
            when(studentRepository.save(any(Student.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

            // Act & Assert
            assertThrows(DataIntegrityViolationException.class, () -> studentService.createStudent(duplicate));
            verify(studentRepository, times(1)).save(duplicate);
        }
    }

    @Nested
    @DisplayName("Update Operations (updateStudent)")
    class UpdateTests {

        @Test
        @DisplayName("Should update student successfully when ID exists")
        void testUpdateStudentSuccess() {
            // Arrange
            Student updateData = new Student("Peter Updated", "peter.updated@example.com", "AI Science", 22);
            Student savedStudent = new Student(1L, "Peter Updated", "peter.updated@example.com", "AI Science", 22);
            when(studentRepository.existsById(1L)).thenReturn(true);
            when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

            // Act
            Optional<Student> result = studentService.updateStudent(1L, updateData);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Peter Updated", result.get().getName());
            assertEquals("peter.updated@example.com", result.get().getEmail());
            verify(studentRepository, times(1)).existsById(1L);
            verify(studentRepository, times(1)).save(updateData);
        }

        @Test
        @DisplayName("Should return empty Optional when updating non-existent student ID")
        void testUpdateStudentNotFound() {
            // Arrange
            Student updateData = new Student("Ghost", "ghost@example.com", "None", 30);
            when(studentRepository.existsById(999L)).thenReturn(false);

            // Act
            Optional<Student> result = studentService.updateStudent(999L, updateData);

            // Assert
            assertFalse(result.isPresent());
            verify(studentRepository, times(1)).existsById(999L);
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return empty Optional when updating with null ID")
        void testUpdateStudentWithNullId() {
            // Arrange
            Student updateData = new Student("Ghost", "ghost@example.com", "None", 30);

            // Act
            Optional<Student> result = studentService.updateStudent(null, updateData);

            // Assert
            assertFalse(result.isPresent());
            verify(studentRepository, never()).existsById(any());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return empty Optional when updating with null data payload")
        void testUpdateStudentWithNullData() {
            // Act
            Optional<Student> result = studentService.updateStudent(1L, null);

            // Assert
            assertFalse(result.isPresent());
            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Operations (deleteStudent)")
    class DeleteTests {

        @Test
        @DisplayName("Should delete student successfully when ID exists")
        void testDeleteStudentSuccess() {
            // Arrange
            when(studentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(studentRepository).deleteById(1L);

            // Act
            boolean deleted = studentService.deleteStudent(1L);

            // Assert
            assertTrue(deleted);
            verify(studentRepository, times(1)).existsById(1L);
            verify(studentRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should return false when deleting non-existent student ID")
        void testDeleteStudentNotFound() {
            // Arrange
            when(studentRepository.existsById(999L)).thenReturn(false);

            // Act
            boolean deleted = studentService.deleteStudent(999L);

            // Assert
            assertFalse(deleted);
            verify(studentRepository, times(1)).existsById(999L);
            verify(studentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should return false when deleting with null ID")
        void testDeleteStudentWithNullId() {
            // Act
            boolean deleted = studentService.deleteStudent(null);

            // Assert
            assertFalse(deleted);
            verify(studentRepository, never()).existsById(any());
            verify(studentRepository, never()).deleteById(any());
        }
    }
}
