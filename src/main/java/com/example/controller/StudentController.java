package com.example.controller;

import com.example.model.Student;
import com.example.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller exposing HTTP CRUD endpoints for Student management.
 * Supports both standard routes (/api/students) and trailing-slash routes (/api/students/).
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * READ ALL: GET /api/students or GET /api/students/
     * Response: 200 OK with JSON array of students
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * READ BY ID: GET /api/students/{id}
     * Response: 200 OK with Student JSON object, or 404 Not Found if missing
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Student not found with ID: " + id));
    }

    /**
     * CREATE: POST /api/students or POST /api/students/
     * Response: 201 Created with newly created Student JSON
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    /**
     * UPDATE: PUT /api/students/{id}
     * Response: 200 OK with updated Student JSON, or 404 Not Found if missing
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        Optional<Student> updatedStudent = studentService.updateStudent(id, student);
        if (updatedStudent.isPresent()) {
            return ResponseEntity.ok(updatedStudent.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Student not found with ID: " + id));
    }

    /**
     * DELETE: DELETE /api/students/{id}
     * Response: 204 No Content on success, or 404 Not Found if missing
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        boolean deleted = studentService.deleteStudent(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Student not found with ID: " + id));
    }
}
