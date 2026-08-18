# 🎓 Student Management System

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate_6-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![MSSQL Server](https://img.shields.io/badge/Microsoft_SQL_Server-2022-CC292B?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server)
[![Tests](https://img.shields.io/badge/Tests-39%20Passed-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **A production-ready, full-stack enterprise Spring Boot application showcasing multi-tier architecture, Spring Data JPA persistence with Microsoft SQL Server, defensive coding practices, and 100% test coverage.**

---

## 👨‍💻 Executive Summary

This repository demonstrates modern Java backend engineering standards for enterprise web applications. Designed following **Domain-Driven Design (DDD)** and **12-Factor App principles**, it manages student academic records through an interactive web dashboard and a robust RESTful API.

### 🌟 Key Highlights for Reviewers & Recruiters:
- **Clean 4-Tier Layered Architecture**: Strict separation of concerns across Presentation (Web UI), Controller, Service, and Repository layers.
- **Enterprise Persistence**: Integrated with **Microsoft SQL Server (MSSQL)** via **Spring Data JPA** & **Hibernate ORM**, leveraging **HikariCP** high-performance connection pooling.
- **Transaction Safety & Data Integrity**: Method-level declarative transactions (`@Transactional`), database constraint enforcement (unique email indices), and defensive null handling.
- **Comprehensive Automated Testing**: **39 automated tests** combining POJO unit tests, isolated business logic testing with **Mockito**, and web slice integration testing with **MockMvc**.
- **12-Factor Cloud Configuration**: Zero hardcoded credentials in source control; parameterized environment variables with profile-based isolation (`dev`, `prod`).

---

## 🏛️ System Architecture

```
                       ┌──────────────────────────────────────────────┐
                       │          Client (Browser / Postman)          │
                       └──────────────────────┬───────────────────────┘
                                              │ HTTP (JSON)
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ Embedded Apache Tomcat Server (Port 8080)                                               │
│   └─ DispatcherServlet (Front Controller)                                               │
│         └─ HandlerMapping (Route Dispatching)                                           │
└─────────────────────────────────────────────┬───────────────────────────────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 1. Controller Layer: StudentController.java                                             │
│    • Handles HTTP verbs (GET, POST, PUT, DELETE)                                        │
│    • Deserializes JSON payloads with Jackson & validates input                          │
│    • Emits canonical HTTP Status Codes (200, 201, 204, 400, 404)                        │
└─────────────────────────────────────────────┬───────────────────────────────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 2. Service Layer: StudentService.java                                                   │
│    • Encapsulates business validation rules & domain workflows                          │
│    • Manages @Transactional(readOnly = true/false) consistency                          │
│    • Performs defensive guards against null pointers & invalid states                   │
└─────────────────────────────────────────────┬───────────────────────────────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 3. Repository Layer: StudentRepository.java                                             │
│    • Extends Spring Data JpaRepository<Student, Long>                                   │
│    • Auto-generates type-safe SQL queries via Spring proxy abstraction                  │
└─────────────────────────────────────────────┬───────────────────────────────────────────┘
                                              │ HikariCP Connection Pool
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ 4. Database: Microsoft SQL Server (MSSQL)                                               │
│    • Tables: 'students' (Identity PK, Unique Email index, Non-nullable attributes)     │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack & Engineering Practices

| Domain | Technologies & Libraries | Key Practices & Design Patterns |
| :--- | :--- | :--- |
| **Language & Framework** | Java 17+, Spring Boot 3.3.0 | Inversion of Control (IoC), Constructor Dependency Injection, Immutability |
| **Web & API Layer** | Spring MVC, Apache Tomcat 10.1, Jackson | RESTful conventions, ResponseEntity builders, Content Negotiation |
| **Data & Persistence** | Spring Data JPA, Hibernate ORM 6.5, HikariCP | Repository Pattern, Declarative Transaction Management (`@Transactional`) |
| **Database** | Microsoft SQL Server (MSSQL 2022) | Identity primary keys, Unique indices, Schema generation (`ddl-auto`) |
| **Testing Suite** | JUnit 5, Mockito 5, Spring MockMvc, AssertJ | Test-Driven Development (TDD), Mocking, Web Slice Testing, Smoke Tests |
| **Frontend UI** | Modern Vanilla JavaScript, CSS3 Design System, HTML5 | Async Fetch API, Real-time DOM filtering, Glassmorphism design tokens |
| **DevOps & Config** | Apache Maven, Spring Profiles (`dev`/`default`), Git | 12-Factor config, Git-ignored credential separation, Environment variables |

---

## 📡 REST API Specification

### Endpoints Overview

| Method | URI | Description | Success Response | Error Responses |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | Retrieve list of all student records | `200 OK` + `[Student]` | — |
| `GET` | `/api/students/{id}` | Retrieve single student by primary key | `200 OK` + `Student` | `404 Not Found` |
| `POST` | `/api/students` | Register a new student | `201 Created` + `Student` | `400 Bad Request` |
| `PUT` | `/api/students/{id}` | Update existing student record | `200 OK` + `Student` | `404 Not Found` |
| `DELETE` | `/api/students/{id}` | Remove student record | `204 No Content` | `404 Not Found` |

### Sample JSON Payloads

#### Create Request (`POST /api/students`)
```json
{
  "name": "Sarah Connor",
  "email": "sarah.connor@cyberdyne.com",
  "course": "Cybersecurity",
  "age": 23
}
```

#### Successful Response (`201 Created`)
```json
{
  "id": 1,
  "name": "Sarah Connor",
  "email": "sarah.connor@cyberdyne.com",
  "course": "Cybersecurity",
  "age": 23
}
```

---

## 🧪 Testing Pyramid & Quality Assurance

The codebase includes **39 automated tests** covering all application tiers with 100% pass rate:

```
                      / \
                     /   \
                    / 1   \   Application Context Smoke Test
                   /-------\  (SpringBootTest)
                  /   12    \  Web Layer Integration Tests
                 /-----------\ (MockMvc HTTP route & JSON tests)
                /     26      \ Unit Tests
               /---------------\ (Mockito Service Mocks + POJO Model Tests)
```

### Test Suite Breakdown

1. **`StudentControllerTest` (12 Tests - Web MVC Slice)**
   - Verifies HTTP status codes (`200`, `201`, `204`, `400`, `404`).
   - Verifies JSON payload serialization and deserialization.
   - Verifies handling of trailing slashes and malformed request bodies.

2. **`StudentServiceTest` (17 Tests - Business Logic & Isolation)**
   - Validates business logic in complete isolation using `@Mock` repository dependencies.
   - Tests edge cases: duplicate email exceptions, empty inputs, null ID guards, and repository `null` returns.

3. **`StudentTest` (9 Tests - Entity Unit Tests)**
   - Validates constructor variations, getter/setter mutations, boundary ages, and `toString` formatting.

4. **`StudentManagementApplicationTests` (1 Test - Smoke Test)**
   - Ensures Spring Boot ApplicationContext boots and dependency wiring is valid.

### Running Tests Locally
```powershell
.\mvnw.cmd clean test
```

---

## 🚀 Getting Started & Local Setup

### Prerequisites
- **Java Development Kit (JDK)**: 17 or higher
- **Microsoft SQL Server**: Local instance (or SQL Server Express / Docker)
- **Database**: Create an empty database named `StudentDB`

### Step 1: Clone Repository
```bash
git clone https://github.com/syogesh999/StudentManageMent.git
cd StudentManageMent
```

### Step 2: Configure Environment (Local Dev)
The repository uses profile-based configuration. Create a local `src/main/resources/application-dev.properties` (or supply environment variables):

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=StudentDB;trustServerCertificate=true;
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```
*(Note: `application-dev.properties` is protected in `.gitignore` to prevent credential leakage).*

### Step 3: Run the Application
```powershell
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Step 4: Access Dashboard & API
- **Web UI Dashboard**: Open [http://localhost:8080/](http://localhost:8080/)
- **API Endpoint**: [http://localhost:8080/api/students](http://localhost:8080/api/students)

---

## 📦 Production Build

To compile, verify tests, and package a standalone executable JAR:

```powershell
.\mvnw.cmd clean package
```

Run the packaged artifact:
```powershell
java -jar target/spring-boot-practice-1.0.0.jar
```

---

## 📂 Project Directory Structure

```text
SpringBootPractice/
├── pom.xml                                   ← Maven POM with Spring Boot & MSSQL dependencies
├── mvnw / mvnw.cmd                           ← Maven wrapper binaries
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── StudentManagementApplication.java ← Main application bootstrap
│   │   │   ├── controller/
│   │   │   │   └── StudentController.java        ← REST Controller (@RestController)
│   │   │   ├── model/
│   │   │   │   └── Student.java                  ← JPA Database Entity (@Entity, @Table)
│   │   │   ├── repository/
│   │   │   │   └── StudentRepository.java        ← Spring Data JPA Repository interface
│   │   │   └── service/
│   │   │       └── StudentService.java           ← Business logic with @Transactional boundaries
│   │   └── resources/
│   │       ├── application.properties            ← Production base template (no secrets)
│   │       └── static/                           ← Frontend client assets
│   │           ├── index.html                    ← Web dashboard interface
│   │           ├── style.css                     ← Clean CSS design system
│   │           └── app.js                        ← Vanilla JS CRUD client
│   └── test/java/com/example/
│       ├── StudentManagementApplicationTests.java← Context load smoke test
│       ├── controller/
│       │   └── StudentControllerTest.java        ← MockMvc web integration tests (12 tests)
│       ├── model/
│       │   └── StudentTest.java                  ← POJO unit tests (9 tests)
│       └── service/
│           └── StudentServiceTest.java           ← Mockito business logic tests (17 tests)
```

---

## 🤝 Contact & Engineering Profile

Developed by **Yogesh Sankranthi**  
- **GitHub**: [@syogesh999](https://github.com/syogesh999)  
- **Repository**: [syogesh999/StudentManageMent](https://github.com/syogesh999/StudentManageMent)
