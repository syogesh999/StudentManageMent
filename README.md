# 🎓 Student Management System — Spring Boot & MSSQL Learning Lab

A clean, production-structured Spring Boot CRUD application featuring a responsive modern Web UI, Spring Data JPA persistence with **Microsoft SQL Server (MSSQL)**, and a test suite with 100% test pass rate.

---

## 🚀 Quick Start

**Prerequisites:** 
- Java 17+
- Microsoft SQL Server (MSSQL 2016+) with a database named `StudentDB`

### 1. Run the Application

#### Windows (PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

#### Linux / macOS
```bash
./mvnw spring-boot:run
```

Open your browser at: **[http://localhost:8080/](http://localhost:8080/)**

---

## 🏗️ Architecture & Request Lifecycle

This application follows standard Spring Boot **Multi-Tier Layered Architecture**:

```
Browser / Client (UI or Postman)
      │
      ▼ HTTP Request (e.g. POST /api/students)
┌────────────────────────────────────────────────────────┐
│ Embedded Apache Tomcat Server (Port 8080)              │
│   └─ DispatcherServlet (Spring Front Controller)       │
│         └─ HandlerMapping (Matches route URL)          │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│ 1. Controller Layer: StudentController.java            │
│    • Handles HTTP verbs, URL paths, JSON parsing       │
│    • Returns ResponseEntity with HTTP Status Codes     │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│ 2. Service Layer: StudentService.java                  │
│    • Manages business logic and validations            │
│    • Controls @Transactional database boundaries       │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│ 3. Data Access Layer: StudentRepository.java           │
│    • Extends JpaRepository<Student, Long>              │
│    • Spring Data JPA auto-generates CRUD SQL queries   │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│ 4. Database: Microsoft SQL Server (MSSQL)              │
│    • Stores records permanently in table: 'students'   │
└────────────────────────────────────────────────────────┘
```

---

## 🌐 Web Application & Dashboard

| URL | Description |
| :-- | :-- |
| `http://localhost:8080/` | Interactive Student Management Dashboard |
| `http://localhost:8080/api/students` | REST API — JSON endpoints |

### Features
- **Full CRUD**: Create, read, update, and delete student records.
- **Persistent Storage**: Data is stored permanently in MSSQL Server (never lost on restart).
- **Client-side Search**: Real-time filtering by name, email, or course.
- **Analytics Cards**: Real-time counts of total students, unique courses, and average age.
- **Validation**: Client-side & database-level unique constraint checks on emails.

---

## 📡 REST API Reference

| HTTP Method | Endpoint | Description | Success Code | Error Code |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/students` | Retrieve all students | `200 OK` | — |
| **GET** | `/api/students/{id}` | Retrieve student by ID | `200 OK` | `404 Not Found` |
| **POST** | `/api/students` | Create a new student | `201 Created` | `400 Bad Request` |
| **PUT** | `/api/students/{id}` | Update existing student | `200 OK` | `404 Not Found` |
| **DELETE** | `/api/students/{id}` | Delete student by ID | `204 No Content` | `404 Not Found` |

### Sample JSON Payloads

#### Create Student (`POST /api/students`)
```json
{
  "name": "Peter Parker",
  "email": "peter.parker@example.com",
  "course": "Computer Science",
  "age": 21
}
```

#### Update Student (`PUT /api/students/1`)
```json
{
  "name": "Peter Parker",
  "email": "peter.parker@newdomain.com",
  "course": "Artificial Intelligence",
  "age": 22
}
```

---

## 📁 Project Structure

```text
SpringBootPractice/
├── pom.xml                                   ← Maven build file & dependencies
├── mvnw / mvnw.cmd                           ← Maven wrapper scripts
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── StudentManagementApplication.java ← Main Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── StudentController.java        ← REST API Controller (@RestController)
│   │   │   ├── model/
│   │   │   │   └── Student.java                  ← Database Entity (@Entity, @Table)
│   │   │   ├── repository/
│   │   │   │   └── StudentRepository.java        ← Spring Data JPA Repository
│   │   │   └── service/
│   │   │       └── StudentService.java           ← Business logic & @Transactional service
│   │   └── resources/
│   │       ├── application.properties            ← Production base configuration template
│   │       ├── application-dev.properties        ← Local dev configuration (git-ignored)
│   │       └── static/                           ← Frontend assets
│   │           ├── index.html                    ← Web dashboard UI
│   │           ├── style.css                     ← CSS design system
│   │           └── app.js                        ← Vanilla JS fetch client
│   └── test/java/com/example/
│       ├── StudentManagementApplicationTests.java← Context load smoke test
│       ├── controller/
│       │   └── StudentControllerTest.java        ← MockMvc REST API tests (12 tests)
│       ├── model/
│       │   └── StudentTest.java                  ← POJO unit tests (9 tests)
│       └── service/
│           └── StudentServiceTest.java           ← Mockito business logic tests (17 tests)
```

---

## 🧑‍💻 Spring Boot Key Concepts for Learners

### 1. Separation of Concerns (Why 4 Layers?)
- **Model (`Student.java`)**: Represents your data structure and database table schema.
- **Repository (`StudentRepository.java`)**: Interacts directly with the database. Spring Data JPA auto-generates SQL queries at runtime without writing SQL boilerplate.
- **Service (`StudentService.java`)**: Contains business rules, validations, and `@Transactional` boundaries. Keeps the controller clean.
- **Controller (`StudentController.java`)**: Handles HTTP concerns (routes, status codes, JSON request/response conversion).

### 2. Essential JPA & Spring Annotations

| Annotation | Where It's Used | What It Does |
| :--- | :--- | :--- |
| `@Entity` | `Student.java` | Tells JPA/Hibernate: "Map this Java class to a database table". |
| `@Table(name = "students")` | `Student.java` | Specifies the exact table name in MSSQL. |
| `@Id` | `Student.java` | Marks the primary key field. |
| `@GeneratedValue(IDENTITY)` | `Student.java` | Delegates auto-increment ID generation to SQL Server's `IDENTITY` column. |
| `@Column(nullable=false, unique=true)` | `Student.java` | Defines column constraints (`NOT NULL`, `UNIQUE`). |
| `@Repository` | `StudentRepository.java` | Marks interface as a Spring Data repository for database access. |
| `@Service` | `StudentService.java` | Registers class as a Spring Service Bean in the ApplicationContext. |
| `@Transactional` | `StudentService.java` | Wraps method execution in a database transaction (commits on success, rollbacks on failure). |
| `@RestController` | `StudentController.java` | Marks class as a REST endpoint handler returning JSON automatically. |

### 3. How Spring Data JPA Saves Code
Instead of writing manual JDBC connections and SQL queries like:
```java
// Traditional JDBC (Old way - 15+ lines of boilerplate per query)
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM students WHERE id = ?");
```
With Spring Data JPA:
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    // That's it! findAll(), findById(), save(), deleteById() are generated automatically!
}
```

### 4. Configuration & Security Best Practice
- **`application.properties`**: Committed to GitHub with environment variable placeholders (`${DB_USERNAME:}`, `${DB_PASSWORD:}`) to ensure zero hardcoded passwords exist in public repositories.
- **`application-dev.properties`**: Kept locally on your computer (listed in `.gitignore`) for local MSSQL connection settings.

---

## 🧪 Running the Automated Tests

To execute the entire test suite of **39 tests**:

```powershell
.\mvnw.cmd clean test
```

### Test Suite Overview:
- **`StudentTest` (9 tests)**: Validates constructors, getters/setters, boundary ages, and `toString`.
- **`StudentServiceTest` (17 tests)**: Unit tests business logic in total isolation using Mockito mocks.
- **`StudentControllerTest` (12 tests)**: Uses `MockMvc` to test HTTP status codes, routing, and JSON serialization.
- **`StudentManagementApplicationTests` (1 test)**: Verifies that the Spring Boot ApplicationContext boots cleanly.

---

## 📦 Building for Production

To compile and package the application into a standalone executable JAR:

```powershell
.\mvnw.cmd clean package
```

Run the packaged JAR:
```powershell
java -jar target/spring-boot-practice-1.0.0.jar
```
