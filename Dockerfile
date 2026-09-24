FROM eclipse-temurin:21-jdk-alpine AS build
FROM maven:3.9.16-clipse-temurin-21 AS mvn
WORKDIR /app
COPY ./code .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=mvn /app/boot/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
