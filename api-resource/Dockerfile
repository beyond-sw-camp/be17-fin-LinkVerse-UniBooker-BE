FROM gradle:8.9-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :api-resource:clean :api-resource:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/api-resource/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]