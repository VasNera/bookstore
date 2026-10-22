# ---- build stage: compile the project with the Maven wrapper ------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only what is needed to resolve the dependencies first, so Docker can cache
# this layer and re-download nothing when only the source code changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

# ---- runtime stage: a slim JRE image with only the jar ------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Do not run the application as root.
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/uploads && chown -R app:app /app
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
