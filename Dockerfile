FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="foodshop@example.com"
LABEL description="Food Shop Application - E-commerce platform with Spring Boot"

# Create app directory
WORKDIR /app

# Copy Maven build artifact
COPY target/food-shop-1.0.0.jar app.jar

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
