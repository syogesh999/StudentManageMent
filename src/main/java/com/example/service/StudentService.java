package com.example.service;

import com.example.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service Layer responsible for Student business logic and in-memory data management.
 * 
 * Why Service Layer exists:
 * - Separation of Concerns: The Controller handles HTTP concerns (parsing URLs, JSON conversion, HTTP statuses).
 *   The Service handles domain business rules (creating IDs, updating records, validating data).
 * - Reusability & Testability: Business logic can be tested independently of HTTP/Web infrastructure.
 * 
 * Note: Data is stored in memory using ConcurrentHashMap. It resets whenever the application restarts!
 */
@Service
public class StudentService {

    // Thread-safe in-memory map storing student ID -> Student object
    private final Map<Long, Student> studentMap = new ConcurrentHashMap<>();
    
    // Thread-safe ID counter initialized for new student creation
    private final AtomicLong idCounter = new AtomicLong(3);

    public StudentService() {
        // Pre-populate with 3 sample fictional students
        studentMap.put(1L, new Student(1L, "Peter Parker", "peter@example.com", "Computer Science", 21));
        studentMap.put(2L, new Student(2L, "Tony Stark", "tony@example.com", "Electrical Engineering", 35));
        studentMap.put(3L, new Student(3L, "Steve Rogers", "steve@example.com", "History & Tactics", 28));
    }

    /**
     * Retrieve all students.
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    /**
     * Retrieve a single student by ID. Safe against null IDs.
     */
    public Optional<Student> getStudentById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(studentMap.get(id));
    }

    /**
     * Create a new student with auto-generated ID.
     */
    public Student createStudent(Student student) {
        Long newId = idCounter.incrementAndGet();
        student.setId(newId);
        studentMap.put(newId, student);
        return student;
    }

    /**
     * Update an existing student by ID. Safe against null IDs.
     */
    public Optional<Student> updateStudent(Long id, Student updatedData) {
        if (id == null || !studentMap.containsKey(id)) {
            return Optional.empty();
        }
        updatedData.setId(id);
        studentMap.put(id, updatedData);
        return Optional.of(updatedData);
    }

    /**
     * Delete a student by ID. Safe against null IDs.
     */
    public boolean deleteStudent(Long id) {
        if (id == null) {
            return false;
        }
        return studentMap.remove(id) != null;
    }
}
