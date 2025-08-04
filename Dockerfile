FROM openjdk:17-jdk

#수정
WORKDIR /app

#JAR 복사
COPY build/libs/*SNAPSHOT.jar app.jar

#GCP 인증 파일 복사
COPY src/main/resources/static/credentials.json /app/credentials.json

#환경변수 등록
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json

COPY .env .env

## 컨테이너가 실행될 때 최초로 실행시키고 싶은 명령어
ENTRYPOINT ["java","-jar","app.jar"]

EXPOSE 8080
