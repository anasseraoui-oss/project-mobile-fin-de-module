# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY auth-server/pom.xml auth-server/
COPY resource-server/pom.xml resource-server/
COPY resource-server/src resource-server/src
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -pl resource-server

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/resource-server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
