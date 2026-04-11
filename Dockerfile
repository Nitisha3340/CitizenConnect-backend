# Multi-stage build: reproducible JDK 17 + fat JAR (Railway / any container host)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/citizenconnect.jar app.jar
# Railway sets PORT; Spring reads server.port=${PORT:8080}
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
USER 65534:65534
ENTRYPOINT ["java", "-jar", "app.jar"]
