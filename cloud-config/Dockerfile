
FROM eclipse-temurin:17-jre
ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9296
ENTRYPOINT ["java", "-jar", "app.jar"]


