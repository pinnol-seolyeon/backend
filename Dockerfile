FROM openjdk:17-jdk

#수정
WORKDIR /app

COPY build/libs/*SNAPSHOT.jar app/finnol-0.0.1-SNAPSHOT.jar

## 컨테이너가 실행될 때 최초로 실행시키고 싶은 명령어
ENTRYPOINT ["java","-jar","app.jar"]

EXPOSE 8080
