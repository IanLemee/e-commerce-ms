FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .

COPY commons-core/pom.xml commons-core/
COPY commons-core/src commons-core/src

COPY stock/pom.xml stock/

RUN mvn -N install

RUN mvn -f commons-core/pom.xml install

COPY stock/src stock/src

RUN mvn -f stock/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/stock/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

