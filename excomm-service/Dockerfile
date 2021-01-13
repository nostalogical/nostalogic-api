FROM openjdk:15-alpine
WORKDIR /home
COPY /build/libs/excomm-service-*-boot.jar excomm-service.jar
RUN apk add curl
HEALTHCHECK --interval=60s --timeout=5s CMD curl -f http://localhost:3603/system/up || exit 1
EXPOSE 3603
ENTRYPOINT ["java", "-jar", "excomm-service.jar"]
