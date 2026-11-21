# Stage 1: Build the Java WAR application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy backend and frontend source directories
COPY backend ./backend
COPY frontend ./frontend

# Build the WAR file inside backend directory (packages servlets + frontend into SmartPorter.war)
WORKDIR /build/backend
RUN mvn clean package -DskipTests

# Stage 2: Serve using Apache Tomcat 9
FROM tomcat:9.0-jdk17-temurin

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy application as ROOT.war so it is served at /
COPY --from=build /build/backend/target/SmartPorter.war /usr/local/tomcat/webapps/ROOT.war

# Copy entrypoint script for dynamic port binding (Render/Railway/Heroku/Fly.io)
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Expose default Tomcat and Render port
EXPOSE 8080 10000

ENTRYPOINT ["docker-entrypoint.sh"]
