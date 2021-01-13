FROM openjdk:15-alpine
WORKDIR /home
COPY /build/libs/content-service-*-boot.jar content-service.jar
RUN apk add curl
HEALTHCHECK --interval=60s --timeout=5s CMD curl -f http://localhost:3604/system/up || exit 1
EXPOSE 3604
ENTRYPOINT ["java", "-jar", "content-service.jar"]
