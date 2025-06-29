# Step 1: Build the application using Maven and Java 21
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies early
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy full source code
COPY src ./src

# Build Spring Boot app
RUN mvn clean package -DskipTests

# Step 2: Run stage (Java 21 runtime)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8180

ENTRYPOINT ["java", "-jar", "app.jar"]
