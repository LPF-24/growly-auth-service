# === Stage 1: build the app ===
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Кэшируем зависимости Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники и собираем приложение
COPY . .
RUN mvn clean package -DskipTests

# === Stage 2: run ===
FROM eclipse-temurin:17
WORKDIR /app

# Копируем jar из предыдущего stage
COPY --from=build /app/target/*.jar app.jar

# Запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]
