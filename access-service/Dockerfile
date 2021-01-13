FROM openjdk:15-alpine
WORKDIR /home
COPY /build/libs/access-service-*-boot.jar access-service.jar
RUN apk add curl
HEALTHCHECK --interval=60s --timeout=5s CMD curl -f http://localhost:3601/system/up || exit 1
EXPOSE 3601
ENTRYPOINT ["java", "-jar", "access-service.jar"]
