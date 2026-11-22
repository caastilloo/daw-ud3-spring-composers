# Etapa 1: compila el proyecto con Maven y genera el fat-jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -q clean package -DskipTests

# Etapa 2: imagen final, solo con el JRE y el jar (sin Maven ni el codigo fuente)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
# Perfil docker: puerto ${PORT:8080} y datos en /app/data/csv
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
