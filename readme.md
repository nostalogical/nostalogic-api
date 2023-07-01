# Nostalogic API

This is the API for the Nostalogic web application. It's written in Kotlin and uses gradle and spring boot with a 
microservice architecture.

# 1. Running

Java 17 and gradle 7.3.3 are currently required for this application.

# 2. Documentation

## 2.1 Technical documentation

This project includes a content service which supports the markdown format. As such, for convenience additional 
documentation is located in the [.docs](.docs) folder. These files are inserted into the content service on startup so 
the Nostalogic web app can be used to view its own documentation.

## 2.2 API documentation

Springdoc OpenAPI is included in this project, which provides auto-generated documentation for controllers. This can be
found at `<host><port>/swagger-ui/index.html`.
