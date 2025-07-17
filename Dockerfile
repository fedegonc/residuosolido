# ========================================
# DOCKERFILE PARA SPRING BOOT - RESIDUO SÓLIDO APP
# ========================================

# ========================================
# STAGE 1: BUILD
# ========================================
FROM maven:3.9.5-openjdk-21-slim AS build

# Establecer directorio de trabajo
WORKDIR /build

# Copiar archivos de configuración de Maven primero (para cache de dependencias)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias (esto se cachea si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación (omitir tests para build más rápido)
RUN ./mvnw clean package -DskipTests -B

# ========================================
# STAGE 2: RUNTIME
# ========================================
FROM openjdk:21-jre-slim AS runtime

# Metadatos de la imagen
LABEL maintainer="fedegonc"
LABEL version="1.0"
LABEL description="Aplicación Spring Boot para gestión de residuos sólidos"

# Crear usuario no-root para seguridad
RUN groupadd -r spring && useradd -r -g spring spring

# Instalar herramientas útiles para debugging (opcional)
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat-traditional \
    && rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=build /build/target/*.jar app.jar

# Crear directorio para logs y uploads
RUN mkdir -p /app/logs /app/uploads && \
    chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring

# Configurar JVM para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
