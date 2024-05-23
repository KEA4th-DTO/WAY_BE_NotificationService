FROM openjdk:17-alpine
COPY build/libs/way.notification-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080