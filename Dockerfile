
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .

RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -DskipTests package


FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/target/*SNAPSHOT.jar /app/app.jar

EXPOSE 8080

RUN java -version

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]
