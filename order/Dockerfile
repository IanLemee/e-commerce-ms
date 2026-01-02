FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .

COPY commons-core/pom.xml commons-core/
COPY commons-core/src commons-core/

COPY order/pom.xml order/
COPY order/src order/src

RUN mvn -N install
RUN mvn -f commons-core/pom.xml install
RUN mvn -f order/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/order/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]