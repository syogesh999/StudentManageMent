package com.example.service;

import com.example.model.Student;
import com.example.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer responsible for Student business logic and transaction management.
 *
 * Architecture:
 * - Handles domain business rules and transaction boundaries.
 * - Delegates database operations to Spring Data JPA repository.
 */
@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Retrieve all students from the database.
     */
    public List<Student> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students != null ? students : Collections.emptyList();
    }

    /**
     * Retrieve a single student by ID.
     */
    public Optional<Student> getStudentById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return studentRepository.findById(id);
    }

    /**
     * Create and persist a new student.
     */
    @Transactional
    public Student createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student payload cannot be null");
        }
        student.setId(null); // Ensure database assigns the auto-increment ID
        return studentRepository.save(student);
    }

    /**
     * Update an existing student by ID.
     */
    @Transactional
    public Optional<Student> updateStudent(Long id, Student updatedData) {
        if (id == null || updatedData == null || !studentRepository.existsById(id)) {
            return Optional.empty();
        }
        updatedData.setId(id); // Enforce path ID
        Student saved = studentRepository.save(updatedData);
        return Optional.of(saved);
    }

    /**
     * Delete a student by ID.
     */
    @Transactional
    public boolean deleteStudent(Long id) {
        if (id == null || !studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }
}
