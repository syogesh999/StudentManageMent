package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity representing a Student stored in the MSSQL 'students' table.
 *
 * JPA Annotations explained:
 *
 * @Entity       - Tells Hibernate/JPA: "this class maps to a database table"
 * @Table        - Specifies the exact table name in the DB ("students")
 * @Id           - Marks the primary key column
 * @GeneratedValue(IDENTITY) - SQL Server auto-increments the ID (IDENTITY column)
 * @Column       - Maps a field to a specific column; nullable=false = NOT NULL constraint
 */
@Entity
@Table(name = "students")
public class Student {

    // Primary Key - SQL Server IDENTITY auto-increments this
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String course;

    @Column(nullable = false)
    private Integer age;

    // -------------------------------------------------------
    // Default No-Args Constructor (Required by JPA spec)
    // -------------------------------------------------------
    public Student() {
    }

    // -------------------------------------------------------
    // Parameterized Constructor
    // -------------------------------------------------------
    public Student(Long id, String name, String email, String course, Integer age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.course = course;
        this.age = age;
    }

    // Constructor without ID (used when creating a new Student; DB assigns ID)
    public Student(String name, String email, String course, Integer age) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.age = age;
    }

    // -------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------
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
