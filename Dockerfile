# syntax=docker/dockerfile:1

############################
# 1) Build stage
############################
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy wrapper + pom first for better caching
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

RUN chmod +x ./mvnw

# Download deps (best-effort caching)
RUN ./mvnw -DskipTests package -q || true

# Copy source
COPY src ./src

RUN ./mvnw -DskipTests package

############################
# 2) Runtime stage
############################
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

