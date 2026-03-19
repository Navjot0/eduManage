# School Management System – Spring Boot REST API

Complete REST API for a school management system backed by PostgreSQL.

## Tech Stack
| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL |
| Auth | JWT (access + refresh tokens) |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven |
| Extras | Lombok, MapStruct |

---

## Setup

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (schema already applied via `DB_SCHEMA_DOCUMENT.sql`)

### 2. Configure `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/school_db
    username: postgres
    password: postgres
```
Or use environment variables:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret-key-in-base64
```

### 3. Run
```bash
mvn spring-boot:run
```

### 4. Swagger UI
```
http://localhost:8080/api/v1/swagger-ui.html
```

---

## Authentication
All protected endpoints require a `Bearer` token in the `Authorization` header.

```
Authorization: Bearer <access_token>
```

---

## API Reference

### 🔐 Auth  `POST /api/v1/auth/...`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/login` | Login, receive access + refresh tokens | ❌ |
| POST | `/auth/refresh` | Refresh access token | ❌ |
| POST | `/auth/logout` | Revoke refresh token | ✅ |
| PUT | `/auth/change-password` | Change own password | ✅ |
| POST | `/auth/forgot-password` | Request OTP for reset | ❌ |
| POST | `/auth/reset-password/{userId}` | Reset password with OTP | ❌ |

---

### 👤 Users  `ADMIN only`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/users?role=` | List all users (filter by role) |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Create user |
| PUT | `/users/{id}` | Update user |
| PATCH | `/users/{id}/toggle-status` | Activate / deactivate |
| DELETE | `/users/{id}` | Delete user |

---

### 🎒 Students
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/students?className=&section=` | ADMIN, TEACHER | List all / filter by class |
| GET | `/students/{id}` | ADMIN, TEACHER, STUDENT | Get by ID |
| GET | `/students/roll/{rollNumber}` | ADMIN, TEACHER | Get by roll number |
| POST | `/students` | ADMIN | Enroll student |
| PUT | `/students/{id}` | ADMIN, TEACHER | Update details |
| DELETE | `/students/{id}` | ADMIN | Remove student |

---

### 👨‍🏫 Teachers
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/teachers?department=` | ADMIN, TEACHER | List all / filter by dept |
| GET | `/teachers/{id}` | ADMIN, TEACHER | Get by ID |
| POST | `/teachers` | ADMIN | Add teacher |
| PUT | `/teachers/{id}` | ADMIN | Update teacher |
| DELETE | `/teachers/{id}` | ADMIN | Remove teacher |

---

### 🏫 Classes
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/classes?academicYear=` | ADMIN, TEACHER | List all |
| GET | `/classes/{id}` | ADMIN, TEACHER | Get by ID |
| POST | `/classes` | ADMIN | Create class |
| PATCH | `/classes/{id}/assign-teacher/{teacherId}` | ADMIN | Assign class teacher |
| DELETE | `/classes/{id}` | ADMIN | Delete class |

---

### 📅 Attendance
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/attendance/student/{id}?from=&to=` | ALL | Student records (date range) |
| GET | `/attendance/student/{id}/summary?from=&to=` | ALL | % summary with counts |
| GET | `/attendance/class?className=&section=&date=` | ADMIN, TEACHER | Class daily attendance |
| POST | `/attendance` | ADMIN, TEACHER | Mark single student |
| POST | `/attendance/bulk` | ADMIN, TEACHER | Mark entire class at once |
| PATCH | `/attendance/{id}?status=&remarks=` | ADMIN, TEACHER | Correct a record |
| DELETE | `/attendance/{id}` | ADMIN | Remove record |

---

### 📝 Assignments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/assignments?className=&section=` | ALL | List by class |
| GET | `/assignments/{id}` | ALL | Get by ID |
| GET | `/assignments/teacher/{teacherId}` | ADMIN, TEACHER | Teacher's assignments |
| POST | `/assignments` | ADMIN, TEACHER | Create assignment |
| PUT | `/assignments/{id}` | ADMIN, TEACHER | Update assignment |
| DELETE | `/assignments/{id}` | ADMIN, TEACHER | Delete assignment |
| GET | `/assignments/{id}/submissions` | ADMIN, TEACHER | All submissions |
| GET | `/assignments/submissions/student/{id}` | ALL | Student's submissions |
| POST | `/assignments/{id}/submit` | STUDENT, ADMIN | Submit assignment |
| PATCH | `/assignments/submissions/{id}/grade` | ADMIN, TEACHER | Grade submission |

---

### 📋 Exams
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/exams?className=&academicYear=` | ALL | List exams |
| GET | `/exams/{id}` | ALL | Get exam |
| POST | `/exams` | ADMIN, TEACHER | Create exam |
| PATCH | `/exams/{id}/status?status=` | ADMIN, TEACHER | Update status |
| DELETE | `/exams/{id}` | ADMIN | Delete exam |
| GET | `/exams/{id}/results` | ADMIN, TEACHER | All results |
| GET | `/exams/{id}/results/student/{sid}` | ALL | Student's results |
| GET | `/exams/results/student/{sid}` | ALL | All results for student |
| POST | `/exams/{id}/results` | ADMIN, TEACHER | Add result |
| PUT | `/exams/results/{id}` | ADMIN, TEACHER | Update result |

---

### 💰 Fees
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/fee-types?activeOnly=` | ALL | List fee types |
| POST | `/fee-types` | ADMIN | Create fee type |
| PATCH | `/fee-types/{id}/toggle` | ADMIN | Toggle active |
| GET | `/fees/student/{id}?academicYear=` | ADMIN, STUDENT | Student's fees |
| GET | `/fees/status/{status}` | ADMIN | By status (overdue etc.) |
| GET | `/fees/{id}` | ADMIN, STUDENT | Get record |
| POST | `/fees` | ADMIN | Create fee record |
| PUT | `/fees/{id}` | ADMIN | Update (record payment) |

---

### 🗓 Timetable
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/timetable/class?className=&section=&academicYear=&day=` | ALL | Class timetable |
| GET | `/timetable/teacher/{id}?day=` | ADMIN, TEACHER | Teacher timetable |
| POST | `/timetable` | ADMIN | Create slot (conflict-checked) |
| DELETE | `/timetable/{id}` | ADMIN | Remove slot |

---

### 📚 Study Materials
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/study-materials?className=&section=&subject=` | ALL | List by class |
| GET | `/study-materials/{id}` | ALL | Get by ID |
| GET | `/study-materials/teacher/{id}` | ADMIN, TEACHER | Teacher's uploads |
| POST | `/study-materials` | ADMIN, TEACHER | Upload material |
| DELETE | `/study-materials/{id}` | ADMIN, TEACHER | Remove (soft delete) |

---

### 📢 Announcements
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/announcements` | ALL | Role-filtered announcements |
| GET | `/announcements/all` | ADMIN | All active |
| GET | `/announcements/{id}` | ALL | Get by ID |
| POST | `/announcements` | ADMIN, TEACHER | Create |
| PUT | `/announcements/{id}` | ADMIN, TEACHER | Update |
| DELETE | `/announcements/{id}` | ADMIN, TEACHER | Deactivate |
| POST | `/announcements/{id}/read` | ALL | Mark as read |

---

### 🔔 Notifications
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/notifications?unreadOnly=` | ALL | My notifications |
| GET | `/notifications/unread-count` | ALL | Unread count badge |
| POST | `/notifications` | ADMIN | Send to user |
| PATCH | `/notifications/{id}/read` | ALL | Mark one read |
| PATCH | `/notifications/read-all` | ALL | Mark all read |
| DELETE | `/notifications/{id}` | ALL | Delete notification |

---

## Project Structure

```
src/main/java/com/school/
├── SchoolManagementApplication.java
├── config/
│   ├── SecurityConfig.java          # JWT stateless security
│   └── OpenApiConfig.java           # Swagger with Bearer auth
├── controller/                      # 12 REST controllers
├── dto/
│   ├── request/                     # 25+ validated request DTOs
│   └── response/                    # ApiResponse<T> + 18 response DTOs
├── entity/                          # 16 JPA entities
├── enums/                           # 11 PostgreSQL enum mirrors
├── exception/
│   └── GlobalExceptionHandler.java  # Structured error responses
├── repository/                      # 18 Spring Data JPA repositories
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserPrincipal.java
└── service/impl/                    # 10 service classes
```

## Role Matrix

| Role | Access |
|---|---|
| `admin` | Full access to all endpoints |
| `teacher` | Read students, mark attendance, manage assignments, add exam results, upload materials |
| `student` | Read own data (attendance, results, assignments, fees, materials) |
