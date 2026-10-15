# Etapa 1: Compilacion y empaquetado
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiar descriptores de Maven y el wrapper
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B || true

# Copiar codigo fuente y construir el jar
COPY src src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen de ejecucion ligera
FROM eclipse-temurin:25-jre
WORKDIR /app

# Crear directorio para subida de fotos (volumen)
RUN mkdir -p /app/uploads

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
