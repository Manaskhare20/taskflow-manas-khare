TaskFlow — Backend (Spring Boot)

🚀 Overview



TaskFlow is a minimal task management backend system that allows users to:



Register and authenticate using JWT

Create and manage projects

Add and manage tasks within projects

Assign tasks and track their status



This project is built with a focus on clean architecture, proper authorization, and production-ready practices.



🛠️ Tech Stack

Java 17

Spring Boot 3

Spring Security + JWT

PostgreSQL

Flyway (database migrations)

Docker \& Docker Compose

Testcontainers (integration testing)

🏗️ Architecture



The application follows a layered architecture:



Controller → Service → Repository → Database



Controller → Handles HTTP requests/responses

Service → Business logic + authorization

Repository → Database interaction

DTOs → API contracts (no entity exposure)

🔐 Authentication

JWT-based authentication

Token expiry: 24 hours

Claims include:

user\_id

email



Passwords are hashed using BCrypt.



🔒 Authorization



Authorization logic is centralized in AuthorizationService.



Key rules:



Project access:

Owner OR assigned to at least one task

Project update/delete:

Owner only

Task update:

Owner OR assignee OR creator

Task delete:

Owner OR creator

🗄️ Database Design

PostgreSQL with Flyway migrations

No Hibernate auto schema generation

Key Tables:

Users

Projects

Tasks

Design Decision:

Added creator\_id in tasks to support delete permissions

⚙️ Running Locally

Prerequisites

Docker installed

Steps

git clone https://github.com/Manaskhare20/taskflow-manas-khare

cd taskflow-manas-khare



cp .env.example .env   # Windows: copy .env.example .env



docker compose up --build

Application URL

http://localhost:8080

🧪 Test Credentials

Email:    test@example.com

Password: password123

📡 API Endpoints

Auth

POST /auth/register

POST /auth/login

Projects

GET /projects

POST /projects

GET /projects/{id}

PATCH /projects/{id}

DELETE /projects/{id}

Tasks

GET /projects/{id}/tasks?status=\&assignee=

POST /projects/{id}/tasks

PATCH /tasks/{id}

DELETE /tasks/{id}

Stats (Bonus)

GET /projects/{id}/stats

📄 Error Handling



All errors follow a consistent format:



{

&#x20; "error": "validation failed",

&#x20; "fields": {

&#x20;   "title": "title is required"

&#x20; }

}

📊 Pagination



Supported via query params:



?page=0\&size=10

🧪 Testing



Integration tests implemented using Testcontainers:



Auth flow

Project + task flow



Run tests:



mvn test

🐳 Docker



The project runs using:



docker compose up



Includes:



PostgreSQL

Backend service

💡 What I'd Do With More Time

Add project membership table for multi-user collaboration

Implement RBAC (role-based access control)

Add Redis caching

Improve test coverage

Add audit logging and soft deletes

Enable real-time updates using WebSockets

⚠️ Notes

.env file is not committed for security reasons

Use .env.example as reference

All configs are environment-driven

👨‍💻 Author



Manas Khare

