# Используем базовый образ OpenJDK
FROM eclipse-temurin:21-jdk
# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем Gradle-артефакты и создаем jar-файл
COPY build/libs/altayboots-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Указываем точку входа
ENTRYPOINT ["java", "-jar", "app.jar"]