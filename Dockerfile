# Stage 1: Build source code với Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code và đóng gói
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Tạo non-root user
RUN useradd --system --create-home --uid 10001 rkverse

# Sửa wildcard tên jar và cấp quyền trực tiếp cho user 10001
COPY --from=build --chown=10001:10001 /workspace/target/*.jar /app/app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]