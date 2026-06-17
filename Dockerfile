# Етап 1: Збірка проєкту за допомогою Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Збираємо .jar файл, пропускаючи тести для швидкості
RUN mvn clean package -DskipTests

# Етап 2: Запуск готового додатка
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]