FROM maven:3.9.6-ecplise-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

COPY notification/pom.xml notification/
COPY notification/src notification/src

RUN mvn -N install
RUN mvn -f notification/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/notification/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]