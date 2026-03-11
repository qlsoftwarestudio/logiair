# -------- Stage 1: build
FROM gradle:8.14.2-jdk21 AS builder

WORKDIR /app
COPY . .

RUN gradle clean build --no-daemon

# -------- Stage 2: runtime liviano
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
