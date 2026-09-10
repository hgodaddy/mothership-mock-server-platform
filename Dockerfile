# MMSP Sprint 1 — Java / Spring Boot container image (linux/amd64 + linux/arm64)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Use non-Alpine JRE — eclipse-temurin:17-jre-alpine has no linux/arm64 manifest
FROM eclipse-temurin:17-jre
WORKDIR /app
USER root
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*
ENV PORT=8080
ENV JAVA_OPTS=""
COPY --from=build /workspace/target/mothership-mock-server-platform-0.1.0.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
