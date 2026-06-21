FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre-alpine
RUN addgroup -S identityforge && adduser -S identityforge -G identityforge
RUN mkdir -p /app/config /app/public/avatars /app/public/exports /app/logs && \
    chown -R identityforge:identityforge /app

WORKDIR /app
COPY --from=build /app/target/identityforge-*.jar app.jar

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

USER identityforge
EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]
