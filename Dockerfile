FROM openjdk:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# credentials.json을 안전한 위치에 복사 (외부에 노출되지 않도록)
COPY src/main/resources/static/credentials.json /app/credentials/credentials.json

# Spring Boot JAR 파일 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 환경 변수 설정 (Google Cloud TTS 사용을 위한 인증)
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/credentials/credentials.json

# 포트 오픈
EXPOSE 8080

# 컨테이너 실행 시 jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
