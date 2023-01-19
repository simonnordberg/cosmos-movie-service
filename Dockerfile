FROM amazoncorretto:17-alpine
COPY target/*-with-dependencies.jar /app/app.jar
EXPOSE 50051
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
