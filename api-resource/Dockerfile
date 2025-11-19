# 1. Build Stage: Gradle을 사용하여 JAR 파일 빌드
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# 캐싱을 위해 루트 설정 파일과 공통 모듈 복사 (필수)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY common ./common

# 11타겟 모듈 복사
COPY api-resource ./api-resource

# api-resource 모듈만 빌드 (-x test: 테스트 생략)
RUN ./gradlew :api-resource:clean :api-resource:build -x test --no-daemon

# 2. Run Stage: 가벼운 런타임 환경 구성
FROM openjdk:17-slim
WORKDIR /app

# 빌드된 Jar 파일 복사 (경로: /app/모듈명/build/libs/*.jar)
COPY --from=builder /app/api-resource/build/libs/*.jar app.jar

EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]