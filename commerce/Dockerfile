# Multi-stage build for commerce (context: repo root cases/novashop)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY platform ./platform
COPY order-return ./order-return
COPY payment-refund ./payment-refund
COPY commerce ./commerce
COPY fulfillment ./fulfillment
COPY demo-web ./demo-web
RUN chmod +x gradlew \
    && ./gradlew :commerce:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/commerce/build/libs/commerce-*.jar /tmp/jars/
RUN JAR="$(find /tmp/jars -name 'commerce-*.jar' ! -name '*-plain.jar' | head -n1)" \
    && cp "$JAR" /app/app.jar \
    && rm -rf /tmp/jars
EXPOSE 8083
HEALTHCHECK --interval=5s --timeout=3s --start-period=40s --retries=12 \
  CMD curl -sf http://localhost:8083/products >/dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
