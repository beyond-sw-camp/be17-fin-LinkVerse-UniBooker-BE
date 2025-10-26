# 빌드
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# 필수 파일만 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 종속성 캐시만 (한 번만)
RUN ./gradlew dependencies --no-daemon --configure-on-demand

# 코드 복사 후 빌드
COPY src ./src
RUN ./gradlew bootJar --no-daemon --configure-on-demand

# 실행
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]
