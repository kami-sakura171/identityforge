# IdentityForge — Customer Identity & Access Management Platform

IdentityForge is an enterprise-grade CIAM platform designed for **air-gapped, on-premise, or network-restricted environments**. It provides complete identity lifecycle management for end users with zero external network dependencies.

## Features

### For Customers
- **Self-service registration** with strict password policies (≥10 chars, 1 uppercase, 1 digit, 1 special)
- **Profile management**: personal info, avatar upload (JPEG/PNG ≤2MB), notification preferences (6 categories)
- **Identity verification** with document upload (AES-256 encrypted at rest)
- **Multi-role switching**: hold multiple contextual roles (Standard User, Verified User, Org Delegate)
- **In-app notifications**: password resets, ToS changes, account lock events
- **Terms of Service** version tracking with consent records

### For Administrators
- **Dashboard**: total users, active sessions, locked accounts, 30-day registration trend
- **Customer management**: search, lock/unlock, force password reset
- **Custom profile fields**: define up to 20 fields (text, dropdown, boolean)
- **ToS management**: publish new versions with auto-deactivation
- **Bulk CSV import**: up to 1,000 accounts with row-level validation
- **Security alerts**: triggered when ≥10 distinct accounts have failed logins in 5 minutes

### Security
- JWT authentication (60-min expiry), bcrypt password hashing (cost 12)
- Account lockout after 5 failed attempts (15-min lock, atomic counter)
- Multi-device session control (max 3 concurrent, oldest invalidated)
- AES-256-GCM encryption for sensitive fields (locally managed key file)
- Input validation, CSRF protection, audit logging

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Frontend | Thymeleaf + Bootstrap 5 (local), Chart.js (local) |
| Database | MySQL 8.0 |
| Auth | JWT (jjwt 0.12) |
| Build | Maven 3.9, Java 17 |
| Deploy | Docker + Docker Compose |

## Quick Start (Docker)

```bash
# Clone and enter directory
cd identityforge

# Start the application
docker compose up -d

# View logs
docker compose logs -f app

# The application will be available at:
# http://localhost:8080
```

### Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `Admin@12345` |

> **Important**: Change the admin password immediately after first login.

## Manual Build & Run

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8.0

### Steps

1. **Configure database** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/identityforge?...
   spring.datasource.username=identityforge
   spring.datasource.password=your_password
   ```

2. **Set JWT secret**:
   ```bash
   export JWT_SECRET="your-long-random-secret-at-least-256-bits"
   ```

3. **Build**:
   ```bash
   mvn clean package -DskipTests
   ```

4. **Run**:
   ```bash
   java -jar target/identityforge-1.0.0.jar
   ```

5. **Access**: http://localhost:8080

## Running Tests

```bash
# Run all tests (unit + API)
bash run_tests.sh

# Run only unit tests
bash unit_tests/test_suite.sh

# Run only API tests (requires running instance)
bash API_tests/test_suite.sh
```

## API Reference

### Authentication
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | None | Register new customer |
| POST | `/api/auth/login` | None | Login, returns JWT |
| POST | `/api/auth/refresh` | None | Refresh access token |
| POST | `/api/auth/logout` | JWT | Invalidate session |
| GET | `/api/auth/validate` | JWT | Validate token |

### Customer Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customer/profile` | Get own profile |
| PUT | `/api/customer/profile` | Update profile |
| PUT | `/api/customer/password` | Change password |
| POST | `/api/customer/avatar` | Upload avatar |
| GET | `/api/customer/notifications` | List notifications |
| PUT | `/api/customer/preferences` | Update notification prefs |
| POST | `/api/customer/verification` | Submit identity verification |
| POST | `/api/customer/roles/switch` | Switch contextual role |
| POST | `/api/customer/consents` | Accept current ToS |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Dashboard statistics |
| GET | `/api/admin/customers` | Search customers |
| PUT | `/api/admin/customers/{id}/lock` | Lock account |
| PUT | `/api/admin/customers/{id}/unlock` | Unlock account |
| POST | `/api/admin/custom-fields` | Create custom field |
| POST | `/api/admin/tos` | Publish new ToS version |
| POST | `/api/admin/import/csv` | Bulk import users |
| PUT | `/api/admin/session-config` | Update session timeout |

Full API documentation: see `docs/api-reference.md`

## Project Structure

```
identityforge/
├── src/main/java/com/identityforge/
│   ├── config/        # Security, JWT, encryption, web config
│   ├── security/      # JWT provider, auth filter, user principal
│   ├── controller/    # REST controllers (auth, customer, admin, common)
│   ├── service/       # Business logic (auth, customer, admin, common)
│   ├── repository/    # Spring Data JPA repositories
│   ├── model/         # JPA entities + enums
│   ├── dto/           # Request/Response DTOs
│   ├── exception/     # Custom exceptions + global handler
│   ├── util/          # Password validator, date utils, CSV parser, hash utils
│   └── scheduler/     # Lockout expiry, session cleanup, security alerts
├── src/main/resources/
│   ├── templates/     # Thymeleaf templates (auth, customer, admin, error)
│   ├── static/        # CSS, JS, images (Bootstrap + Chart.js local)
│   └── application.properties
├── unit_tests/        # Shell scripts for unit test execution
├── API_tests/         # Shell scripts for API endpoint testing
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | MySQL host |
| `DB_USER` | identityforge | MySQL username |
| `DB_PASSWORD` | idforge_pass | MySQL password |
| `JWT_SECRET` | (default) | JWT signing secret (min 256 bits) |
| `AVATAR_STORAGE_PATH` | public/avatars | Avatar file storage path |

## License

Internal use. All rights reserved.
