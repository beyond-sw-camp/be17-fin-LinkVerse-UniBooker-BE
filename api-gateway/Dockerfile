FROM gradle:8.9-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :api-gateway:clean :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]