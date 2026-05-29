# Build stage
# syntax=docker/dockerfile:1.7
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms
COPY . .

# Build resource-server with explicit annotation processor configuration
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
