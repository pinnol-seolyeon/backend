FROM openjdk:17-jdk

COPY build/libs/*SNAPSHOT.jar app.jar

## 컨테이너가 실행될 때 최초로 실행시키고 싶은 명령어
ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8080