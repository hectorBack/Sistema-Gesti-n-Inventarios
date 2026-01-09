# Etapa 1: Construcción (Build) utilizando Eclipse Temurin y Maven
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Runtime) utilizando Eclipse Temurin
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 6464
ENTRYPOINT ["java", "-jar", "app.jar"]