# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only pom first to leverage Docker cache for deps
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
# to skip tests: -DskipTests
COPY src ./src
RUN mvn -B package

# Stage 2: Runtime image
FROM azul/zulu-openjdk:21-latest
WORKDIR /app

# Copy packaged JAR from builder
COPY --from=builder /app/target/http-server.jar ./http-server.jar

# Create folder for JFR recordings
RUN mkdir -p /app/recordings
RUN mkdir -p /app/logs

# Expose your server port
EXPOSE 4221

# Start server with JFR enabled
ENTRYPOINT ["java","-XX:StartFlightRecording=filename=/app/recordings/server.jfr,settings=profile,dumponexit=true,delay=1s","-jar","http-server.jar"]
