FROM eclipse-temurin:21-jdk AS build
WORKDIR /home/gradle/src
COPY . .
RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/TarotBackend.jar
ENTRYPOINT ["java", "-jar", "/app/TarotBackend.jar"]