FROM openjdk:16-jdk-buster

COPY target/*.jar app.jar
COPY application.yml application.yml

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]