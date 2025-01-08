#Build stage
FROM gradle:latest AS build
WORKDIR /app
COPY . . 
RUN gradle jar

# Package stage
FROM eclipse-temurin:latest AS base
WORKDIR /app
COPY --from=BUILD /app/app/build/libs/app.jar .

FROM base AS client
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM base AS diary
ENTRYPOINT ["java", "-cp", "app.jar", "n7.HagiMule.Diary.DiaryImpl"]