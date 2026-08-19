package com.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensive Unit tests for Student model POJO covering constructors, getters, setters,
 * edge cases (nulls, boundary ages, special characters), and toString formatting.
 */
@DisplayName("Student Model Unit Tests")
class StudentTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create student using full constructor with valid parameters")
        void testFullConstructor() {
            Student student = new Student(10L, "Wanda Maximoff", "wanda@example.com", "Physics", 25);
            
            assertEquals(10L, student.getId());
            assertEquals("Wanda Maximoff", student.getName());
            assertEquals("wanda@example.com", student.getEmail());
            assertEquals("Physics", student.getCourse());
            assertEquals(25, student.getAge());
        }

        @Test
        @DisplayName("Should create student using constructor without ID for new entries")
        void testConstructorWithoutId() {
            Student student = new Student("Vision", "vision@example.com", "Robotics", 3);
            
            assertNull(student.getId());
            assertEquals("Vision", student.getName());
            assertEquals("vision@example.com", student.getEmail());
            assertEquals("Robotics", student.getCourse());
            assertEquals(3, student.getAge());
        }

        @Test
        @DisplayName("Should create empty student using default no-args constructor")
        void testNoArgsConstructor() {
            Student student = new Student();
            
            assertNull(student.getId());
            assertNull(student.getName());
            assertNull(student.getEmail());
            assertNull(student.getCourse());
            assertNull(student.getAge());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Mutation Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should update all properties independently using setters")
        void testSetters() {
            Student student = new Student();
            student.setId(5L);
            student.setName("Sam Wilson");
            student.setEmail("sam@example.com");
            student.setCourse("Aeronautics");
            student.setAge(32);

            assertEquals(5L, student.getId());
            assertEquals("Sam Wilson", student.getName());
            assertEquals("sam@example.com", student.getEmail());
            assertEquals("Aeronautics", student.getCourse());
            assertEquals(32, student.getAge());
        }

        @Test
        @DisplayName("Should overwrite existing property values correctly")
        void testOverwriteSetters() {
            Student student = new Student(1L, "Initial Name", "initial@example.com", "Math", 20);
            
            student.setName("Updated Name");
            student.setEmail("updated@example.com");
            student.setCourse("Advanced Math");
            student.setAge(21);

            assertEquals("Updated Name", student.getName());
            assertEquals("updated@example.com", student.getEmail());
            assertEquals("Advanced Math", student.getCourse());
            assertEquals(21, student.getAge());
        }
    }

    @Nested
    @DisplayName("Edge Case & Special Character Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle names and courses with special characters and accents")
        void testSpecialCharacters() {
            Student student = new Student(15L, "Renée O'Connor-Smith", "renee.o'connor@example-domain.org", "C++ & AI Systems", 22);
            
            assertEquals("Renée O'Connor-Smith", student.getName());
            assertEquals("renee.o'connor@example-domain.org", student.getEmail());
            assertEquals("C++ & AI Systems", student.getCourse());
        }

        @Test
        @DisplayName("Should handle boundary numeric ages (e.g. 10, 100, 0)")
        void testBoundaryAges() {
            Student youngStudent = new Student(1L, "Young Prodigy", "young@example.com", "Math", 10);
            Student seniorStudent = new Student(2L, "Senior Learner", "senior@example.com", "History", 100);
            Student zeroStudent = new Student(3L, "Baby Student", "baby@example.com", "Nursery", 0);

            assertEquals(10, youngStudent.getAge());
            assertEquals(100, seniorStudent.getAge());
            assertEquals(0, zeroStudent.getAge());
        }

        @Test
        @DisplayName("Should accept null values for optional/uninitialized fields")
        void testNullFields() {
            Student student = new Student(null, null, null, null, null);
            
            assertNull(student.getId());
            assertNull(student.getName());
            assertNull(student.getEmail());
            assertNull(student.getCourse());
            assertNull(student.getAge());
        }

        @Test
        @DisplayName("Should accept empty string values for name, email, and course")
        void shouldHandleEmptyStringFields() {
            Student student = new Student(1L, "", "", "", 20);

            assertEquals("", student.getName());
            assertEquals("", student.getEmail());
            assertEquals("", student.getCourse());
        }

        @Test
        @DisplayName("Should accept whitespace-only string values without trimming")
        void shouldHandleWhitespaceOnlyFields() {
            Student student = new Student(1L, "   ", "   ", "   ", 20);

            assertEquals("   ", student.getName());
            assertEquals("   ", student.getEmail());
            assertEquals("   ", student.getCourse());
        }

        @Test
        @DisplayName("Should store very long string values without truncation")
        void shouldHandleVeryLongStringValues() {
            String longName = "A".repeat(1000);
            Student student = new Student(1L, longName, "long@example.com", "Course", 25);

            assertEquals(1000, student.getName().length());
            assertEquals(longName, student.getName());
        }

        @Test
        @DisplayName("Should handle Unicode CJK characters and emoji in fields")
        void shouldHandleUnicodeAndEmojiCharacters() {
            Student student = new Student(1L, "田中太郎", "tanaka@example.jp", "数学コース", 22);
            assertEquals("田中太郎", student.getName());
            assertEquals("数学コース", student.getCourse());

            Student emojiStudent = new Student(2L, "😎 Cool Student", "cool@example.com", "Art 🎨", 19);
            assertEquals("😎 Cool Student", emojiStudent.getName());
            assertEquals("Art 🎨", emojiStudent.getCourse());
        }

        @Test
        @DisplayName("Should accept negative and very large age values at POJO level")
        void shouldHandleNegativeAndLargeAgeValues() {
            Student negAge = new Student(1L, "Neg", "neg@example.com", "Math", -1);
            assertEquals(-1, negAge.getAge());

            Student maxAge = new Student(2L, "Max", "max@example.com", "Math", Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, maxAge.getAge());
        }
    }

    @Nested
    @DisplayName("toString Formatting Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should format toString containing all student field values")
        void testToString() {
            Student student = new Student(1L, "Peter Parker", "peter@example.com", "Computer Science", 21);
            String str = student.toString();
            
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("Peter Parker"));
            assertTrue(str.contains("peter@example.com"));
            assertTrue(str.contains("Computer Science"));
            assertTrue(str.contains("age=21"));
        }
    }
}

