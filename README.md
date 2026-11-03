# UCUA Spring Boot REST API

Spring Boot 3.4.0 (Kotlin) backend for the UCUA (U-SEE U-ACT) Android mobile app.

## Tech Stack

- **Spring Boot** 3.4.0 (Compatible with Spring Boot 4.x patterns)
- **Kotlin** 1.9.25
- **Gradle** (Kotlin DSL)
- **WAR Packaging**
- **YAML Configuration**
- **Spring Data JPA**
- **Spring Security + JWT**
- **SQLite** (embedded database)

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | - | Authenticate, returns Bearer token + user profile |
| POST | `/api/auth/register` | - | Employee self-registration |
| POST | `/api/auth/forgot` | - | Password recovery request |
| GET | `/api/auth/me` | Bearer | Current user profile |
| GET | `/api/dashboard` | Bearer | KPI counters, trend chart, activity feed |
| GET | `/api/lookups` | Bearer | Locations, offences, companies, departments |
| GET | `/api/reports?type=UC` | Bearer | List UC reports |
| GET | `/api/reports?type=UA` | Bearer | List UA reports |
| GET | `/api/reports/{id}` | Bearer | Single report + photos + timeline |
| POST | `/api/reports` | Bearer | Create a UC/UA report or draft |
| PUT | `/api/reports/{id}` | Bearer | Full update (owner or admin) |
| PATCH | `/api/reports/{id}` | Bearer | Partial update (owner or admin) |
| DELETE | `/api/reports/{id}` | Bearer | Delete report + photos (owner or admin) |
| GET | `/api/reports/{id}/photos` | Bearer | List photo evidence |
| POST | `/api/reports/photos` | Bearer | Upload photo evidence |
| POST | `/api/workflow` | Bearer | Approve / reject / close / resubmit |
| GET | `/api/health` | - | Health check |

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:./ucua.db

jwt:
  secret: your-secret-key-here

storage:
  upload-dir: /path/to/uploads
```

Or use an environment variable for the database path:

```bash
export SQLITE_PATH=/path/to/ucua.db
./gradlew bootRun
```

## Build & Run

```bash
# Build WAR
./gradlew bootWar

# Run locally
./gradlew bootRun

# Deploy WAR to Tomcat/Jetty
# Copy build/libs/*.war to webapps/
```

## Database

The application uses **SQLite** as an embedded database. The database file is created automatically on first run (`ddl-auto: update`). No external database server is required.

To migrate data from the legacy MySQL database, use a tool like `sqlite3` or a data migration script to export MySQL data and import into SQLite.

## Security

- Passwords verified against legacy MD5 hash for backward compatibility
- JWT tokens with configurable expiry (default 7 days)
- Role-based access control matching legacy access levels
- CORS enabled for mobile app communication
