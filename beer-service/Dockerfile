FROM eclipse-temurin:17-jre as builder
WORKDIR application
ARG PROJECT_BUILD_FINAL_NAME
ARG PROJECT_ARTIFACT_ID
ARG PROJECT_ID
ADD target/${PROJECT_BUILD_FINAL_NAME}.jar ${PROJECT_BUILD_FINAL_NAME}.jar
RUN java -Djarmode=layertools -jar ${PROJECT_BUILD_FINAL_NAME}.jar extract

FROM eclipse-temurin:17-jre
ARG PROJECT_ARTIFACT_ID
ARG PROJECT_ID
LABEL PROJECT_NAME=${PROJECT_ARTIFACT_ID} \
      PROJECT=${PROJECT_ID}

EXPOSE 8080

# RUN true   added between COPY statement to prevent docker error: faild to load layer....
WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
RUN true
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]