package com.example.repository;

import com.example.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Student persistence.
 *
 * Why just an interface? Spring Data JPA auto-generates the full implementation
 * at startup — no SQL or boilerplate code needed from us.
 *
 * By extending JpaRepository<Student, Long> we get these methods for FREE:
 *
 *  findAll()              -> SELECT * FROM students
 *  findById(id)           -> SELECT * FROM students WHERE id = ?
 *  save(student)          -> INSERT or UPDATE (Hibernate decides based on ID)
 *  deleteById(id)         -> DELETE FROM students WHERE id = ?
 *  existsById(id)         -> SELECT COUNT(*) FROM students WHERE id = ?
 *  count()                -> SELECT COUNT(*) FROM students
 *
 * JpaRepository<Student, Long>:
 *   - Student -> the Entity class this repository manages
 *   - Long    -> the data type of the primary key (@Id field)
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // No code needed here! Spring Data JPA generates everything automatically.
    // You can add custom query methods here later, e.g.:
    //   List<Student> findByCourse(String course);
    //   Optional<Student> findByEmail(String email);

}
