# Task Tracker API

RESTful Spring Boot API for managing projects and tasks with role-based access control.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Security with JWT authentication
- Spring Data JPA and Hibernate
- H2 in-memory database for development
- MapStruct
- Lombok
- Swagger/OpenAPI
- JUnit and Mockito

## How to Run

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

Useful development URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

H2 settings:

- JDBC URL: `jdbc:h2:mem:task_tracker`
- Username: `sa`
- Password: empty

Optional environment variables:

```bash
JWT_SECRET=replace-with-a-long-secret
JWT_EXPIRATION_MS=86400000
```

## Authentication

Use `POST /api/auth/register` to create a user and `POST /api/auth/login` to get a JWT.

Registration accepts an optional `role`. If omitted, the role defaults to `USER`.

Send the token on protected endpoints:

```http
Authorization: Bearer <token>
```

Passwords are stored with BCrypt hashes. JWTs are signed with HS256 using `app.jwt.secret`.

## Roles and Permissions

- `ADMIN`: Full access to projects and tasks.
- `MANAGER`: Can create and manage only their own projects and tasks inside those projects.
- `USER`: Can view their assigned tasks and update status for their assigned tasks.


## API Summary

### Auth

- `POST /api/auth/register` - register a user
- `POST /api/auth/login` - authenticate and receive JWT

### Projects

Project endpoints require `ADMIN` or `MANAGER`.

- `GET /api/projects` - list accessible projects
- `GET /api/projects/{id}` - get project by id
- `POST /api/projects` - create project
- `PUT /api/projects/{id}` - update project
- `DELETE /api/projects/{id}` - delete project

`ownerId` in project requests is optional. Managers can only use their own id. Admins can create or transfer a project for any admin/manager owner.

### Tasks

- `GET /api/tasks` - paginated task listing
- `GET /api/tasks/{id}` - get task by id
- `POST /api/tasks` - create task, requires `ADMIN` or `MANAGER`
- `PUT /api/tasks/{id}` - update task details, requires `ADMIN` or owning `MANAGER`
- `PATCH /api/tasks/{id}/assignee` - assign task, requires `ADMIN` or owning `MANAGER`
- `PATCH /api/tasks/{id}/status` - update status, allowed for assigned user or admin
- `DELETE /api/tasks/{id}` - delete task, requires `ADMIN` or owning `MANAGER`

Task listing supports:

- Pagination: `page`, `size`, `sort`
- Filtering: `projectId`, `assignedUserId`, `status`, `priority`

Examples:

```http
GET /api/tasks?page=0&size=10&status=TODO
GET /api/tasks?projectId=1&priority=HIGH
GET /api/tasks?assignedUserId=3
```

## Request Examples

Register:

```json
{
  "email": "manager@example.com",
  "password": "password123",
  "role": "MANAGER"
}
```

Create project:

```json
{
  "name": "Interview Project",
  "description": "Demo project for task tracking"
}
```

Create task:

```json
{
  "title": "Build API",
  "description": "Implement task tracker requirements",
  "status": "TODO",
  "dueDate": "2026-08-01",
  "priority": "HIGH",
  "projectId": 1,
  "assignedUserId": 3
}
```

Update task status:

```json
{
  "status": "IN_PROGRESS"
}
```

## Postman Collection

The Postman collection is included at:

```text
postman/Task_Tracker_API.postman_collection.json
```

Import it into Postman and set collection variables as needed:

- `baseUrl`: defaults to `http://localhost:8080`
- `token`: set automatically by the login/register examples, or manually
- `projectId`, `taskId`, `userId`: sample ids used by request paths/bodies

## Tests

Run unit tests:

```bash
./mvnw test
```
