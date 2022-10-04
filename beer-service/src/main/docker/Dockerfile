FROM eclipse-temurin:17-jre as builder
WORKDIR application
ADD maven/${project.build.finalName}.jar ${project.build.finalName}.jar
RUN java -Djarmode=layertools -jar ${project.build.finalName}.jar extract

FROM eclipse-temurin:17-jre
LABEL PROJECT_NAME=${project.artifactId} \
      PROJECT=${project.id}

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