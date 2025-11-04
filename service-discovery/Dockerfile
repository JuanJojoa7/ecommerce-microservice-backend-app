
FROM eclipse-temurin:17-jre
ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
ENV SPRING_PROFILES_ACTIVE=dev
COPY . .
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} service-discovery.jar
EXPOSE 8761
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "service-discovery.jar"]


