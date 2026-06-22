# Build stage
# syntax=docker/dockerfile:1.7
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent and module POMs first for dependency caching.
COPY pom.xml .
COPY auth-server/pom.xml auth-server/
COPY resource-server/pom.xml resource-server/
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy only source needed by this image; do not send target/ or unrelated files.
COPY resource-server/src resource-server/src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests \
    -pl resource-server \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=info

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/resource-server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
