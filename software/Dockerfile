ARG JAVA_VERSION=25

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

WORKDIR /extracao

COPY --from=build /workspace/aplicacao.jar ./aplicacao.jar

RUN java -Djarmode=tools -jar aplicacao.jar extract --layers --launcher --destination extraido


FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

RUN addgroup -S servicetrack && adduser -S servicetrack -G servicetrack \
    && apk add --no-cache wget

WORKDIR /app

COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/dependencies/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/spring-boot-loader/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/snapshot-dependencies/ ./
COPY --from=extracao --chown=servicetrack:servicetrack /extracao/extraido/application/ ./

USER servicetrack

ENV JDK_JAVA_OPTIONS="\
-XX:MaxRAMPercentage=75 \
-XX:InitialRAMPercentage=50 \
-XX:+UseSerialGC \
-XX:+ExitOnOutOfMemoryError \
-Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
