FROM gradle:7.6.0-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:17-jdk-slim

EXPOSE 8080

ENV TOKEN=MTA0NDM5MzYxOTU0NjcyMjQzNA.GdXNAu.1xzctEV4MYbjtl1fKSWVOh2ALHC_NPWO__Ai7M

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/GameBot-1.0.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/GameBot-1.0.jar"]