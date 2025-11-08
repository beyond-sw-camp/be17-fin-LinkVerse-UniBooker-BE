FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 결과물만 복사
COPY build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]
