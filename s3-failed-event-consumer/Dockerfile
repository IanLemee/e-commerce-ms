FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY s3-failed-event-consumer/pom.xml s3-failed-event-consumer/

RUN mvn -N install

COPY s3-failed-event-consumer/src s3-failed-event-consumer/src

RUN mvn -f s3-failed-event-consumer/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build app/s3-failed-event-consumer/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]