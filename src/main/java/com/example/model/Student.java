package com.example.model;

/**
 * Model class representing a Student entity in memory.
 * 
 * Fields explained:
 * - id: Unique numeric identifier for each student (Long)
 * - name: Full name of the student (String)
 * - email: Email address of the student (String)
 * - course: Course/major enrolled by the student (String)
 * - age: Age of the student (Integer)
 */
public class Student {

    private Long id;
    private String name;
    private String email;
    private String course;
    private Integer age;

    // Default No-Args Constructor (Required for Jackson JSON deserialization)
    public Student() {
    }

    // Parameterized Constructor
    public Student(Long id, String name, String email, String course, Integer age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.course = course;
        this.age = age;
    }

    // Constructor without ID (used when creating a new Student before ID assignment)
    public Student(String name, String email, String course, Integer age) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.age = age;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", course='" + course + '\'' +
                ", age=" + age +
                '}';
    }
}
