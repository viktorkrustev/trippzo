# ИЗПОЛЗВАМЕ JAVA 21 ЗА КОМПИЛАЦИЯ
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# ИЗПОЛЗВАМЕ JAVA 21 ЗА СТАРТИРАНЕ
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]