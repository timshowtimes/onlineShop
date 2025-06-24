FROM eclipse-temurin:21-jdk
LABEL authors="Admin"

WORKDIR /app

COPY target/*.jar app.jar
COPY src/main/resources/application.properties .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]