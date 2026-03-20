# School Management System – Spring Boot REST API

Complete REST API for a school management system backed by PostgreSQL, with JWT authentication, role-based access control, and full CRUD for all entities.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.3 |
| Language | Java 17 |
| Database | PostgreSQL 14+ |
| Auth | JWT — access token (24h) + refresh token (7d) |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven |
| Extras | Lombok, HikariCP connection pool |

---

## Quick Start

### 1. Prerequisites
- Java 17+, Maven 3.8+, PostgreSQL 14+
- Apply schema: `DB_SCHEMA_DOCUMENT.sql`

### 2. Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/school_db` |
| `DB_USERNAME` | DB username | `postgres` |
| `DB_PASSWORD` | DB password | `postgres` |
| `JWT_SECRET` | Base64-encoded 256-bit secret | built-in dev secret |
| `PORT` | Server port (Railway injects this) | `8080` |

### 3. Run
```bash
mvn spring-boot:run
```

### 4. Create default admin on first startup
Configured via `application.properties`:
```properties
app.admin.email=admin@school.com
app.admin.password=admin123
app.admin.name=Super Admin
```

### 5. Fix passwords after seed data
```
POST http://localhost:8080/api/v1/dev/fix-passwords?password=password123
```

### 6. Swagger UI
```
http://localhost:8080/api/v1/swagger-ui.html
```

---

## Deployment (Railway)

Always use `https://` — Railway handles SSL termination.
```
https://your-app.up.railway.app/api/v1/auth/login   ✅
http://your-app.up.railway.app/api/v1/auth/login    ❌
```
Add `server.port=${PORT:8080}` in `application.properties`.

---

## Authentication

```
Authorization: Bearer <access_token>
```

- Login → get `accessToken` + `refreshToken`
- On 401 → call `/auth/refresh` to renew
- Deactivated users are blocked immediately even with a valid token (checked on every request)

---

## Role Matrix

| Role | Access |
|---|---|
| `admin` | Full access to all endpoints |
| `teacher` | Mark attendance, manage assignments, add results, upload materials |
| `student` | Read own data — attendance, results, fees, assignments, materials |

---

## API Reference

Base: `http://localhost:8080/api/v1`

### 🔐 Auth `/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Login |
| POST | `/auth/refresh` | Public | Refresh token |
| POST | `/auth/logout` | Bearer | Revoke refresh token |
| PUT | `/auth/change-password` | Bearer | Change password |
| POST | `/auth/forgot-password` | Public | Request reset OTP |
| POST | `/auth/reset-password/{userId}` | Public | Reset with OTP |

---

### 👤 Users `/users`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/users` | ADMIN | All users |
| GET | `/users?role=teacher` | ADMIN | Filter by role |
| GET | `/users?isActive=true` | ADMIN | Filter by active status |
| GET | `/users?role=student&isActive=false` | ADMIN | Combined filter |
| GET | `/users/{id}` | ADMIN | Get by ID |
| POST | `/users` | **Public** | Create user |
| PUT | `/users/{id}` | ADMIN | Update user |
| PATCH | `/users/{id}/toggle-status` | ADMIN | Toggle active ↔ inactive |
| PATCH | `/users/{id}/activate` | ADMIN | Activate user |
| PATCH | `/users/{id}/deactivate` | ADMIN | Deactivate user |
| DELETE | `/users/{id}` | ADMIN | Delete user |

---

### 🎒 Students `/students`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/students` | ADMIN, TEACHER | All students |
| GET | `/students?className=10&section=A` | ADMIN, TEACHER | Filter by class |
| GET | `/students?status=active` | ADMIN, TEACHER | Filter by status |
| GET | `/students?className=10&section=A&status=inactive` | ADMIN, TEACHER | Combined filter |
| GET | `/students/me` | Bearer | Own profile (student JWT) |
| GET | `/students/{id}` | ADMIN, TEACHER | Get by student ID |
| GET | `/students/roll/{rollNumber}` | ADMIN, TEACHER | Get by roll number |
| POST | `/students` | ADMIN | Enroll student |
| PUT | `/students/{id}` | ADMIN, TEACHER | Update student |
| PATCH | `/students/{id}/toggle-status` | ADMIN | Toggle active ↔ inactive |
| PATCH | `/students/{id}/activate` | ADMIN | Activate (enables login + class count +1) |
| PATCH | `/students/{id}/deactivate` | ADMIN | Deactivate (disables login + class count -1) |
| DELETE | `/students/{id}` | ADMIN | Delete student |

---

### 👨‍🏫 Teachers `/teachers`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/teachers` | ADMIN, TEACHER | All teachers |
| GET | `/teachers?department=Science` | ADMIN, TEACHER | Filter by department |
| GET | `/teachers?status=active` | ADMIN, TEACHER | Filter by status |
| GET | `/teachers?department=Science&status=active` | ADMIN, TEACHER | Combined filter |
| GET | `/teachers/me` | Bearer | Own profile (teacher JWT) |
| GET | `/teachers/by-user/{userId}` | ADMIN, TEACHER | Get by user ID |
| GET | `/teachers/{id}` | ADMIN, TEACHER | Get by teacher ID |
| POST | `/teachers` | ADMIN | Add teacher |
| PUT | `/teachers/{id}` | ADMIN | Update teacher |
| PATCH | `/teachers/{id}/toggle-status` | ADMIN | Toggle active ↔ inactive |
| PATCH | `/teachers/{id}/activate` | ADMIN | Activate (enables login) |
| PATCH | `/teachers/{id}/deactivate` | ADMIN | Deactivate (disables login) |
| DELETE | `/teachers/{id}` | ADMIN | Delete teacher |

---

### 🏫 Classes `/classes`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/classes` | ADMIN, TEACHER | All classes |
| GET | `/classes?academicYear=2024-25` | ADMIN, TEACHER | Filter by year |
| GET | `/classes/{id}` | ADMIN, TEACHER | Get by ID |
| POST | `/classes` | ADMIN | Create class |
| PATCH | `/classes/{id}/assign-teacher/{teacherId}` | ADMIN | Assign class teacher |
| POST | `/classes/{id}/sync-count` | ADMIN | Re-sync student_count |
| DELETE | `/classes/{id}` | ADMIN | Delete class |

---

### 📅 Attendance `/attendance`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/attendance/student/{id}` | ALL | Student records |
| GET | `/attendance/student/{id}?from=&to=` | ALL | Date range filter |
| GET | `/attendance/student/{id}/summary?from=&to=` | ALL | % summary |
| GET | `/attendance/class?className=10&section=A&date=` | ADMIN, TEACHER | Class by date |
| POST | `/attendance` | ADMIN, TEACHER | Mark single student |
| POST | `/attendance/bulk` | ADMIN, TEACHER | Mark entire class |
| PATCH | `/attendance/{id}?status=&remarks=` | ADMIN, TEACHER | Correct record |
| DELETE | `/attendance/{id}` | ADMIN | Delete record |

---

### 📝 Assignments `/assignments`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/assignments?className=10&section=A` | ALL | By class |
| GET | `/assignments/{id}` | ALL | By ID |
| GET | `/assignments/teacher/{teacherId}` | ADMIN, TEACHER | Teacher's assignments |
| POST | `/assignments` | ADMIN, TEACHER | Create |
| PUT | `/assignments/{id}` | ADMIN, TEACHER | Update |
| DELETE | `/assignments/{id}` | ADMIN, TEACHER | Delete |
| GET | `/assignments/{id}/submissions` | ADMIN, TEACHER | All submissions |
| GET | `/assignments/submissions/student/{id}` | ALL | Student's submissions |
| POST | `/assignments/{id}/submit` | STUDENT, ADMIN | Submit |
| PATCH | `/assignments/submissions/{id}/grade` | ADMIN, TEACHER | Grade |

---

### 📋 Exams `/exams`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/exams` | ALL | All exams |
| GET | `/exams?className=10&academicYear=2024-25` | ALL | Filter |
| GET | `/exams/{id}` | ALL | By ID |
| POST | `/exams` | ADMIN, TEACHER | Create |
| PATCH | `/exams/{id}/status?status=ongoing` | ADMIN, TEACHER | Update status |
| DELETE | `/exams/{id}` | ADMIN | Delete |
| GET | `/exams/{examId}/results` | ADMIN, TEACHER | All results |
| GET | `/exams/{examId}/results/student/{id}` | ALL | Student results |
| GET | `/exams/results/student/{id}` | ALL | All results for student |
| POST | `/exams/{examId}/results` | ADMIN, TEACHER | Add result |
| PUT | `/exams/results/{resultId}` | ADMIN, TEACHER | Update result |

---

### 💰 Fees

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/fee-types` | ALL | All fee types |
| GET | `/fee-types?activeOnly=true` | ALL | Active only |
| POST | `/fee-types` | ADMIN | Create fee type |
| PATCH | `/fee-types/{id}/toggle` | ADMIN | Toggle active |
| GET | `/fees/student/{id}` | ADMIN, STUDENT | Student fees |
| GET | `/fees/student/{id}?academicYear=2024-25` | ADMIN, STUDENT | Filter by year |
| GET | `/fees/status/overdue` | ADMIN | Overdue fees |
| GET | `/fees/status/pending` | ADMIN | Pending fees |
| GET | `/fees/{id}` | ADMIN, STUDENT | By ID |
| POST | `/fees` | ADMIN | Create record |
| PUT | `/fees/{id}` | ADMIN | Update / record payment |

---

### 🗓 Timetable `/timetable`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/timetable/class?className=10&section=A&academicYear=2024-25` | ALL | Class timetable |
| GET | `/timetable/class?...&day=Monday` | ALL | Filter by day |
| GET | `/timetable/teacher/{id}` | ADMIN, TEACHER | Teacher timetable |
| GET | `/timetable/teacher/{id}?day=Monday` | ADMIN, TEACHER | Filter by day |
| POST | `/timetable` | ADMIN | Create slot (conflict-checked) |
| DELETE | `/timetable/{id}` | ADMIN | Remove slot |

---

### 📚 Study Materials `/study-materials`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/study-materials?className=10&section=A` | ALL | By class |
| GET | `/study-materials?className=10&section=A&subject=Mathematics` | ALL | By subject |
| GET | `/study-materials/{id}` | ALL | By ID |
| GET | `/study-materials/teacher/{id}` | ADMIN, TEACHER | Teacher uploads |
| POST | `/study-materials` | ADMIN, TEACHER | Upload material |
| DELETE | `/study-materials/{id}` | ADMIN, TEACHER | Remove (soft delete) |

---

### 📢 Announcements `/announcements`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/announcements` | Bearer | Role-filtered list |
| GET | `/announcements/all` | ADMIN | All active |
| GET | `/announcements/{id}` | Bearer | By ID |
| POST | `/announcements` | ADMIN, TEACHER | Create |
| PUT | `/announcements/{id}` | ADMIN, TEACHER | Update |
| DELETE | `/announcements/{id}` | ADMIN, TEACHER | Deactivate |
| POST | `/announcements/{id}/read` | Bearer | Mark as read |

---

### 🔔 Notifications `/notifications`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/notifications` | Bearer | My notifications |
| GET | `/notifications?unreadOnly=true` | Bearer | Unread only |
| GET | `/notifications/unread-count` | Bearer | Badge count |
| POST | `/notifications` | ADMIN | Send to user |
| PATCH | `/notifications/{id}/read` | Bearer | Mark one read |
| PATCH | `/notifications/read-all` | Bearer | Mark all read |
| DELETE | `/notifications/{id}` | Bearer | Delete |

---

### 🛠 Dev Tools `/dev`

> **Remove before production.**

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/dev/hash?password=abc` | Public | Generate BCrypt hash |
| POST | `/dev/fix-passwords?password=abc` | Public | Fix all user passwords in DB |

---

## Status Management

| Action | `status` field | `users.is_active` | `classes.student_count` |
|---|---|---|---|
| Activate student | `active` | `true` | +1 |
| Deactivate student | `inactive` | `false` | -1 |
| Activate teacher | `active` | `true` | — |
| Deactivate teacher | `inactive` | `false` | — |

---

## Project Structure

```
src/main/java/com/school/
├── config/           AdminBootstrap, CorsConfig, DataInitializer, OpenApiConfig, SecurityConfig
├── controller/       13 REST controllers
├── dto/              26 request DTOs + 18 response DTOs + ApiResponse<T>
├── entity/           16 JPA entities
├── enums/            11 PostgreSQL ENUM mirrors
├── exception/        GlobalExceptionHandler (404, 405, 400, 401, 403, 409, 500)
├── repository/       18 Spring Data JPA repositories
├── security/         JwtAuthenticationFilter, JwtTokenProvider, UserPrincipal
└── service/impl/     11 service classes
```

---

## Seed Data Credentials

Password for all accounts: **`password123`** (run `/dev/fix-passwords` first)

| Role | Email |
|---|---|
| Admin | admin@school.com |
| Teacher | rajesh.kumar@school.com |
| Teacher | priya.sharma@school.com |
| Student | aarav.mehta@student.com |
| Student | ananya.gupta@student.com |
