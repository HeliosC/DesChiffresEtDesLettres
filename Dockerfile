FROM gradle:8.13-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :server:buildFatJar --no-daemon --stacktrace --info

FROM eclipse-temurin:21-jre-alpine
RUN mkdir /app
WORKDIR /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/server-fat.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/server-fat.jar"]