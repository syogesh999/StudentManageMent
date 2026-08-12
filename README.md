# 🎓 Student Management System — Spring Boot Learning Lab

A minimal, clean Spring Boot Student CRUD application with a modern flat-design web UI.

---

## 🚀 Quick Start

**Prerequisites:** Java 17+

### Windows (PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS
```bash
./mvnw spring-boot:run
```

Open → **http://localhost:8080/**

---

## 🌐 Web Application

| URL | Description |
| :-- | :-- |
| `http://localhost:8080/` | Student Management Dashboard |
| `http://localhost:8080/api/students` | REST API — all students |

### Features
- Add, search, edit, and delete student records
- Real-time client-side form validation
- Live search by name, email or course
- Statistics: total students, unique courses, average age
- Responsive layout (desktop table + mobile card view)
- Success / error toast notifications

---

## 📡 REST API Reference

| Method | Endpoint | Description | Response |
| :----- | :------- | :---------- | :------- |
| GET    | `/api/students`      | Get all students | `200 OK` |
| GET    | `/api/students/{id}` | Get student by ID | `200 OK` / `404` |
| POST   | `/api/students`      | Create new student | `201 Created` |
| PUT    | `/api/students/{id}` | Update student | `200 OK` / `404` |
| DELETE | `/api/students/{id}` | Delete student | `204 No Content` / `404` |

### Example — POST request body
```json
{
  "name": "Peter Parker",
  "email": "peter@example.com",
  "course": "Computer Science",
  "age": 21
}
```

---

## 🧪 Running Tests

```powershell
.\mvnw.cmd clean test
```

**Test Coverage: 53 tests across 3 suites:**
- `StudentTest` — 11 model unit tests
- `StudentServiceTest` — 17 service/business logic tests
- `SpringBootPracticeApplicationTests` — 25 integration / MockMvc tests

---

## 📦 Building a JAR

```powershell
.\mvnw.cmd clean package
java -jar target/spring-boot-practice-1.0.0.jar
```

---

## 📁 Project Structure

```text
SpringBootPractice/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── SpringBootPracticeApplication.java
│   │   │   ├── controller/
│   │   │   │   └── StudentController.java
│   │   │   ├── model/
│   │   │   │   └── Student.java
│   │   │   └── service/
│   │   │       └── StudentService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html   ← Flat-design Student Management Dashboard
│   │           ├── style.css    ← Design system & responsive styles
│   │           └── app.js       ← Vanilla JS CRUD client
│   └── test/java/com/example/
│       ├── SpringBootPracticeApplicationTests.java
│       ├── model/StudentTest.java
│       └── service/StudentServiceTest.java
```

---

## 🧑‍💻 Spring Boot Learning Concepts

> The web UI is intentionally kept clean and user-focused.
> All Spring Boot learning notes live here in the README.

### 1. Request Lifecycle

Every browser action triggers this flow inside Spring Boot:

```
Browser
  └─ fetch('/api/students', { method: 'POST', ... })
        └─ HTTP Request (TCP/IP)
              └─ Tomcat (Embedded Servlet Container)
                    └─ DispatcherServlet  ← Front Controller Pattern
                          └─ HandlerMapping  ← Finds @RequestMapping
                                └─ StudentController  ← Handles the route
                                      └─ StudentService  ← Business logic
                                            └─ ConcurrentHashMap  ← In-memory data store
                                                  └─ Returns Student object
                                                        └─ Jackson → JSON response
                                                              └─ HTTP Response → Browser
```

### 2. Key Spring Annotations

| Annotation | Purpose |
| :--------- | :------ |
| `@SpringBootApplication` | Enables component scanning, auto-configuration, Spring Boot context |
| `@RestController` | Marks class as a REST controller (combines `@Controller` + `@ResponseBody`) |
| `@RequestMapping` | Maps URL base path to the controller |
| `@GetMapping` | Maps HTTP GET requests |
| `@PostMapping` | Maps HTTP POST requests |
| `@PutMapping` | Maps HTTP PUT requests |
| `@DeleteMapping` | Maps HTTP DELETE requests |
| `@PathVariable` | Extracts URL path segments (e.g. `/api/students/{id}`) |
| `@RequestBody` | Deserializes incoming JSON body into a Java object |
| `@Service` | Marks class as a Spring-managed service bean |

### 3. Separation of Concerns

```
Controller     → HTTP concerns (routing, status codes, JSON in/out)
     ↓
Service        → Business logic (ID generation, validation rules, data mutations)
     ↓
Data Store     → ConcurrentHashMap (thread-safe in-memory store; resets on restart)
```

### 4. Data Storage Note

Student data is stored in a `ConcurrentHashMap<Long, Student>` inside `StudentService`.
It is **in-memory only** — data resets every time the application restarts.
This is intentional for learning. To persist data, you would add Spring Data JPA + a database.

### 5. HTTP Status Codes Used

| Operation | Status Code | Meaning |
| :-------- | :---------- | :------ |
| Read all  | `200 OK` | Success with body |
| Read one  | `200 OK` / `404 Not Found` | Found or not found |
| Create    | `201 Created` | New resource created |
| Update    | `200 OK` / `404 Not Found` | Updated or not found |
| Delete    | `204 No Content` / `404 Not Found` | Deleted or not found |
| Bad input | `400 Bad Request` | Malformed JSON or type mismatch |

### 6. DispatcherServlet

`DispatcherServlet` is Spring MVC's **Front Controller**.
It receives every HTTP request from Tomcat and routes it to the correct `@RestController` method using `HandlerMapping`.
You do not write it — Spring Boot registers it automatically when you add `spring-boot-starter-web`.
