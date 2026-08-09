# syntax=docker/dockerfile:1
############################
# Build stage
############################
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy wrapper + pom first so dependency layer is cached separately from source changes
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# --- GitHub Packages auth ---
# Even "public" GitHub Packages repos require authenticated Maven requests.
# We inject a settings.xml at build time via a BuildKit secret so the token
# never gets baked into an image layer.
#
# Build with:
#   DOCKER_BUILDKIT=1 docker compose build notification-service
#
# and make sure GITHUB_TOKEN / GITHUB_USERNAME are set in your shell or .env
# (see docker-compose.yml secrets block).
RUN --mount=type=secret,id=gh_username \
    --mount=type=secret,id=gh_token \
    mkdir -p /root/.m2 && \
    GH_USER=$(cat /run/secrets/gh_username 2>/dev/null || echo "x-access-token") && \
    GH_TOKEN=$(cat /run/secrets/gh_token 2>/dev/null || echo "") && \
    printf '<settings>\n  <servers>\n    <server>\n      <id>github</id>\n      <username>%s</username>\n      <password>%s</password>\n    </server>\n  </servers>\n</settings>\n' "$GH_USER" "$GH_TOKEN" > /root/.m2/settings.xml

COPY src ./src

# If the GitHub package repo genuinely never needs a token, this still works
# fine, since settings.xml with a blank password is harmless for public reads
# that don't require auth.
RUN ./mvnw clean package -DskipTests -s /root/.m2/settings.xml

############################
# Runtime stage
############################
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8010

ENTRYPOINT ["java", "-jar", "app.jar"]
