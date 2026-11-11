ARG JAVA_VERSION=25
ARG OTEL_AGENT_VERSION=2.31.1

FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test

RUN mv build/libs/*-SNAPSHOT.jar /workspace/aplicacao.jar


FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS extracao

ARG OTEL_AGENT_VERSION

WORKDIR /extracao

COPY --from=build /workspace/aplicacao.jar ./aplicacao.jar

RUN java -Djarmode=tools -jar aplicacao.jar extract --layers --launcher --destination extraido

RUN apk add --no-cache curl && \
    curl -fsSL --retry 5 --retry-all-errors --retry-delay 3 \
      -o /extracao/opentelemetry-javaagent.jar \
      "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" && \
    test -s /extracao/opentelemetry-javaagent.jar


FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

RUN addgroup -S servicetrack && adduser -S servicetrack -G servicetrack \
    && apk add --no-cache wget

WORKDIR /app

COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/dependencies/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/spring-boot-loader/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/snapshot-dependencies/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/application/ ./
COPY --from=extracao --chown=servicetrack:servicetrack --chmod=444 /extracao/opentelemetry-javaagent.jar ./opentelemetry-javaagent.jar

USER servicetrack

ENV OTEL_JAVAAGENT_ENABLED=false
ENV OTEL_SERVICE_NAME=service-track-usuarios-veiculos

ENV JDK_JAVA_OPTIONS="\
-XX:MaxRAMPercentage=60 \
-XX:InitialRAMPercentage=25 \
-XX:+UseSerialGC \
-XX:+ExitOnOutOfMemoryError \
-Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "org.springframework.boot.loader.launch.JarLauncher"]
