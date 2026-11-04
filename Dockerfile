# ---------- Build Stage ----------
# Uses the Gradle base image (Gradle 8.11 + JDK 21) directly — no wrapper JAR required.
FROM gradle:8.11-jdk21 AS builder

WORKDIR /app

# Copy build files first for better layer caching
COPY build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src/ ./src/

# Build the executable WAR (uses system Gradle from the image, not the wrapper)
RUN gradle bootWar --no-daemon -x test

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

LABEL org.opencontainers.image.title="ucua-springboot"
LABEL org.opencontainers.image.description="UCUA Spring Boot Application"
LABEL org.opencontainers.image.source="https://github.com/harrysuke/ucua-springboot"

WORKDIR /app

# Create directories for SQLite DB and uploads (mounted as volumes in compose)
RUN mkdir -p /data/db /data/uploads

# Copy the built WAR from the builder stage
COPY --from=builder /app/build/libs/*.war app.war

# Create a non-root user for security
RUN groupadd -r ucua && useradd -r -g ucua -d /app -s /sbin/nologin ucua \
  && chown -R ucua:ucua /app
USER ucua

# Environment defaults
ENV SQLITE_PATH=/data/db/ucua.db
ENV UPLOAD_DIR=/data/uploads
ENV JWT_SECRET=change-this-ucua-mobile-secret-2026
ENV SERVER_PORT=8080

EXPOSE 8080

# Health check using Spring Boot Actuator (curl is present in temurin base image)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ucua/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.war"]
