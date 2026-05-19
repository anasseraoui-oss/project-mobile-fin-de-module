# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY auth-server/pom.xml auth-server/
COPY resource-server/pom.xml resource-server/
RUN mvn dependency:go-offline -B
COPY auth-server/src auth-server/src
RUN mvn package -DskipTests -pl auth-server

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/auth-server/target/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
