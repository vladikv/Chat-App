# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependencies first: this layer is reused until pom.xml changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

# Then the sources (tests run in CI, not here)
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as an unprivileged user; /app/uploads is where user files are stored
RUN groupadd --system app \
    && useradd --system --gid app --no-create-home app \
    && mkdir -p /app/uploads \
    && chown -R app:app /app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 8081

# Container-aware heap sizing; override with JAVA_OPTS if needed
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

# No actuator in the project, so just check that the port accepts connections
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8081"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]