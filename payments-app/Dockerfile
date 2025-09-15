FROM eclipse-temurin:21-jdk
LABEL authors="Timur"

WORKDIR /app

COPY target/payments-app-0.0.6-SNAPSHOT.jar app.jar
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]