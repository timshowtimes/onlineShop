FROM eclipse-temurin:21-jdk
LABEL authors="Timur"

WORKDIR /app

EXPOSE 8888

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]