FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# The Gradle version comes only from gradle/wrapper/gradle-wrapper.properties
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Exit on OOM so the container is restarted instead of running in a broken state.
# Add e.g. -XX:MaxRAMPercentage=70 via JAVA_OPTS in docker-compose to tune the heap.
ENV JAVA_OPTS="-XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# 127.0.0.1 on purpose: the app runs with preferIPv4Stack, "localhost" may resolve to ::1.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.net.preferIPv4Stack=true -jar app.jar"]
