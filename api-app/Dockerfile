FROM gradle:8.9-jdk17 AS builder
WORKDIR /app

# 1) Gradle 설정 파일만 먼저 복사 → 캐시 최대 활용
COPY settings.gradle build.gradle gradle.properties /app/
COPY gradle /app/gradle

# 각 모듈의 build.gradle, common의 build.gradle 먼저 복사 (의존성 캐시)
COPY api-app/build.gradle /app/api-app/build.gradle
COPY common/build.gradle /app/common/build.gradle

# 2) 의존성만 먼저 다운로드 (common 수정 없으면 여기 캐시 재사용됨)
RUN gradle :api-app:dependencies --no-daemon

# 3) 실제 소스 코드 복사 (변경된 모듈만 캐시 깨짐)
COPY api-app /app/api-app
COPY common /app/common

# 4) 모듈 단위 빌드
RUN gradle :api-app:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/api-app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
