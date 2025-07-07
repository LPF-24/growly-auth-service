# Auth Service

`auth-service` handles user authentication and token issuance for the **Growly** microservices ecosystem. It is responsible for login, registration, and JWT-based authentication.

## 📌 Responsibilities

- User registration and login
- Password encoding and validation
- Issuing JWT access tokens
- Token validation for downstream services
- Role-based access control
- Token refresh endpoint (if enabled)

## 🔐 Authentication

- Stateless authentication via **JWT**
- Access tokens are signed and verified using a secret key
- Token is passed via `Authorization: Bearer <token>`

## 🧪 Examples of endpoints

| Method | Endpoint        | Description        |
|--------|------------------|--------------------|
| `POST` | `/auth/registration` | Register a new user |
| `POST` | `/auth/login`    | Authenticate and receive JWT |
| `POST` | `/auth/refresh`  | (Optional) Refresh JWT token |

> Full OpenAPI (Swagger) documentation available at `/swagger-ui.html` (if enabled).

## 🛠 Technologies

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Validation
- Docker
- Kafka
- Redis
- PostgreSQL
- OpenAPI / Swagger
- JUnit 5
- Mockito
- Spring Boot Test, MockMvc

## ⚙️ Configuration

Environment variables required (in `.env` or Docker Compose):

```env
JWT_SECRET=internship
JWT_EXPIRATION_MS=3600000
```

## 👥 Sample Accounts

| Role  | Username    | Password   |
|-------|-------------|------------|
| Admin | `test2`     | `Test234!` |
| User  | `jorge_doe` | `Zegh576!` |