# 실행 이미지
FROM openjdk:17-jdk-slim

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]