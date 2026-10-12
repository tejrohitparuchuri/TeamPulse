# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests && \
    find target/ -maxdepth 1 -name "*.jar" ! -name "*.original" -exec cp {} app.jar \;

# Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/app.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -Xmx384m -Dserver.port=${PORT} -jar app.jar"]
