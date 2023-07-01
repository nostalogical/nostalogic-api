# Modules & Structure

## Structure

### Common

The _common_ module contains classes and code shared across all services, and is included in them as a dependency.
Communication between services is handled through regular REST requests, with the payloads and helper service
communication ("comms") classes being located in the common module.




### Generic module layout
```
|--<module name>
  |--/gradle/
  |--/src/
     |--/integration-test/
     |--/main/
        |--/kotlin/
           |--/net/
              |--/nostalogic/
                 |--/<service>/
                    |--/controllers/
                    |--/persistence/
                    |--/services/
                    |--<service>Application.kt
        |--/resources/
           |--/sql/
              |--/00_schema/
                 |--001_<schema>_service_schema.sql
              |--/01_tables/
                 |--101_<schema>_tables.sql
              |--/02_data/
                 |--201_<schema>_defaults.sql
           |--application.yml
     |--/test/
  |--build.gradle.kts
```

## Module creation process

1. Creation module file structure
2. Create schema SQL files
3. Create persistence classes
4. Create service outline
5. Confirm module application runs
6. Create unit test outline
7. Implement services
8. Implement unit tests
9. Create and implement integration tests

# Naming conventions

## Packages

Packages should be all lower case and plural where relevant, e.g. `users` rather than `user`. Words should be separated
with hyphens.

### Standard names

* `constants`
  * Enums and classes containing literal constants
* `controllers`
* `datamodel`
  * DTO classes for both internal data transfer, and parameters and responses for API requests
* `mappers`
  * Classes/objects for converting one class type to another, such as an entity to a DTO
* `persistence`
  * Entities and repositories
* `services`
* `validators`
  * Classes/objects for confirming a DTO contains valid data

