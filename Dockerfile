
FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY *.json ./
RUN mvn -B package -DskipTests


FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/*.json ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]