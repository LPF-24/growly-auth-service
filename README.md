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

## 📚 Documentation

Interactive API available at:
```
http://localhost:8081/swagger-ui.html
```

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

## 📚 Notes
JWT is validated internally — no external auth call

User data (e.g., userId) is extracted from the JWT

Works behind gateway-service

## ⚙️ Configuration

Environment variables required (in `.env` or Docker Compose):

```env
JWT_SECRET=internship
JWT_EXPIRATION_MS=3600000
```

## 👥 Sample Accounts (http://localhost:5173/login)

| Role  | Username | Password   |
|-------|----------|------------|
| Admin | `admin`  | `ChangeMe_123!` |
| User  | `user`   | `user123!` |

## 🔗 Related
Part of the [growly-infra](https://github.com/LPF-24/growly-infra) project.

---

> 🔐 **auth-service** — keeps your habits safe.