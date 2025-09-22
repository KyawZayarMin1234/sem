FROM openjdk:18
COPY target/sem-0.1.0.1-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]