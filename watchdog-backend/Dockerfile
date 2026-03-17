FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom files
COPY ../pom.xml ../pom.xml
COPY pom.xml .

# Download dependencies (cached layer)
RUN apk add --no-cache maven && mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S watchdog && adduser -S watchdog -G watchdog
USER watchdog

COPY --from=builder /app/target/watchdog-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "app.jar"]
