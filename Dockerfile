# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
# Download dependencies (better layer caching)
RUN mvn -q -e -DskipTests dependency:go-offline
# Copy sources and build
COPY src ./src
RUN mvn -q -e -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Use the fat jar built above
COPY --from=build /workspace/target/*SNAPSHOT.jar /app/app.jar
# Expose HTTP port
EXPOSE 8080
# Show active Java version
RUN java -version
# Run the app; allow extra JVM args via JAVA_OPTS
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]
