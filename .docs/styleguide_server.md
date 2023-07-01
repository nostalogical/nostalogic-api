# Server Styleguide

1. The API should be self-contained and self-explanatory. A third party with little knowledge should be able to operate it.
    1. API should supply all its relevant error messages and have documentation.
2. The API should use plurals for endpoints, e.g. `/api/users`.
3. No file extensions should be exposed in URLs.
4. All user-targeted error messages should be translatable phrases
5. On updating an entity and null or unchanged field should be ignored.
6. To avoid "dirtying" the DTO, deletion or clearing of a field should be represented by either an empty string `""` or a predefined value like `"CLEAR"`.
7. Null values should not be included in response payloads.
8. For convenience, all API methods that can reasonably support it should allow bulk actions, such as deleting a list or changing multiple statuses.
    1. Standard entity creation or update requests should always apply only to a single entity.
    2. Bulk endpoints should return a list of results, and generally always have the status code 200.

## Searches

1. All searches should use GET requests, and any filters should be supplied as URL parameters.
2. All search results should use a standard format, including a list of search results, the page number and size, and the total number of results across all pages.

# Entities

1. All entities should have a unique UUID, this should be a 36 character lower case string in the pattern `8-4-4-4-12`.
2. An entity can be referred to with an `EntityReference`, the form of which varies based on the context.
    1. An `EntitySignature` is a full, unambiguous reference to a specific entity with its type prefixed, e.g. `USER_77c52c87-f46e-4c12-8eb0-b6a701f0ba36`.
    2. A `LocalReference` is a shorter way of referring to an entity, when the type is already known, e.g. `77c52c87-f46e-4c12-8eb0-b6a701f0ba36` can be used when it's implicit that the type is `USER`.
    3. A `LocalReference` can also be just the entity type, e.g. `USER`, to support referring to all entities of a specific type.
3. For deletion, actual deletion of entities should be avoided in favour of "deleted" flags or alternate tables.
    1. For something like a membership, where an entry just links two tables, deletion is fine as long as changes are recorded somewhere.
    2. For most proper entities deletion should be handled by marking an entity as deleted, although this means allowing "undelete" when this blocks creation of entities.
    3. Where marking an entity as deleted is inappropriate, `deleted_` tables can be used. These reflect the original table, but with no constraints or indices, and before an entity is deleted a copy is transferred into the deleted table so all info is retained and possible to restore.

# Database

1. The database structure and content for each service should defined by SQL scripts within their code.
2. SQL scripts should be divided into folders based on purpose (such as defining the schema, tables, and data) and into files based on logical divisions such as time of creation. These files and folders should all use numerical prefixes for ordering followed by descriptive names.
3. On service start up, the SQL scripts should all be executed in order of their prefixes.
    1. Scripts should therefore be written in a repeatable, non-destructive way. E.g. using `IF NOT EXISTS` for table creation, and `ON CONFLICT DO NOTHING` when inserting default values.
4. Integration tests should also use the standard SQL scripts to create a database when they're run, but support overrides or additional scripts based on their prefixes.
5. For dev readability, all enumerations should be stored as strings in the database.

## Tables

1. All entities should use lower case table and column names with words separated by underscores.
2. All entities should include the columns `created`, `creator_id`, and `tenant`.
3. Foreign keys should be used where appropriate within a schema, but this should not apply outside to entities outside the service.
4. Each service should include a `mod_entity` table, for recording the modification history of all entities within that schema.
    1. This table should include the entity type, entity ID, and the standard `created` and `creator_id` columns to identify the modifier and modification time. There should also be two optional text columns; `original` to optionally store the pre-change value (either a raw text value or full entity JSON), and `notes` to record anything else that may be relevant.

# Validation

1. Validation should be handled by devoted validation classes.
2. If a field is invalid an error should be thrown including the reason it's invalid, e.g. "<field> is too short, must be over 3 characters long". This should be translated on the server side so the error message can be passed directly to the user.
3. A list of all initial validation errors should be returned rather than throwing one at a time.

# Error Handling

1. All "handled" errors from the server should have 4 standard parts.
    1. *User message*: A friendly, translated message that can be exposed directly to the user.
    2. *Debug message*: A more detailed, untranslated message, aimed directly at developers using the API.
    3. *Status*: A HTTP status code, the same as the code on the response itself.
    4. *Error code*: A unique integer code corresponding to the point the error was thrown from.
2. The **error code** is intended to allow quick searching for exact origin of an error in the code, or reference in a list to an error's cause. It's composed of 3 parts: `<Service prefix><Error type code><Instance code>`:
    - **Service prefix** is a 2 digit code identifying the service the error was throw in:
        1. Base prefix: 01
        2. Access prefix: 02
        3. User prefix: 03
        4. Excomm prefix: 04
        5. Content prefix: 05
        6. File prefix: 06
    - **Error type code** is a 2 digit code identifying the type of error thrown:
        1. Access error (usually a 403 response): 01
        2. Authentication error (usually a 401): 02
        3. Delete error (usually a 404): 03
        4. Retrieve error (usually a 404): 04
        5. Save error (usually a 500): 05
        6. Security error (usually a 500): 06
        7. Validation error (usually a 400): 07
    - **Instance code** is a 3 digit number indicating a specific instance of an error type within a service.
    - For example, 201008: This represents the 8th instance (*008*) of an access error (*01*) being thrown within the access service (*2*).

# Security

## Access Control

1. Access rights are handled by a devoted service, and all access checks should involve a call to this service.
    1. Per user API call, as few rights checks as possible should be made. E.g. if the possible rights required by an endpoint are known, they should all be requested in the initial access check and covered in a single access report.
2. Access checks involve the user's token being sent to the access service.
    1. An invalid session (e.g. being logged out) should cause the user to be treated as logged out.
    2. When a user's rights change (e.g. they join a new group) this must be updated in their current session to allow the change to immediately take effect, and to avoid the access service needing to call the user's service for group information.
3. Rights for a user are derived from any rights defined for them specifically, and any rights defined for groups they're part of.

## Sessions

1. Sessions should be managed in the database, so it should be possible to log a user out directly in the database.
2. A user can supply their authentication with either a cookie (default for the browser) or a bearer token. Bearer token takes precedence so it can be used for debugging purposes.

## User Security

1. Any login failures should return an opaque response, e.g. "Invalid login & password combination", in order to not reveal if the username or email used exists.
2. Registration attempts with an existing email (re-registering) should appear the same as a normal registration but trigger a "password reset" style prompt to the registered user.
3. Usernames don't count for enumeration. Existence checks on registration are fine.
4. Any personal information (such as real name or address) should be stored on a separate object so it can be hidden by default.

## Tokens & Codes

1. A JWT token should be decodable into a "grant". All grants should extend a base implementation and the token should be able to determine its grant type.
2. A code should not be tied to specific type of value. E.g. a long phrase or a 6 character code should both be usable for the same purpose.

# Inter-service

1. Communication between services should be language agnostic.
2. Inter service call depth should be limited to 1 as much as possible. *This is to limit potential timeouts and keep complexity within calls down.*
    1. Content service calling the users service and then the access service is fine.
    2. Content service calling the users service, which in turn calls the access service, should be avoided.

# Architecture

1. The micro-services should not be tied to a specific language.
    1. Within each language, generic functionality (such as settings, security, inter-service communication) should use base versions where possible. E.g. if one service is built in python, the components required for all services should be easily reusable.
2. All services should be designed around unit tests and integration test, first built to pass their tests.
    1. Unit tests should focus on smaller, confined sections of logic to check for "expected results".
    2. Integration tests should be confined to a single service and mock any communication required with other services, such as the access service, but use a real database. Any required rights should be mocked by the test.
3. Configurable server settings should be held in memory.
    1. Settings should also be stored in the database to persist through restarts.
    2. Live updating/reloading of the in memory settings should be possible.
4. Each service should have a securable internal API, through which settings can be configured and other internal operations managed.
    1. These internal service APIs should be accessed through a dedicated admin service, and so should follow a consistent standard format.

# Server Setup

1. Servers should follow a consistently themed naming convention.
    1. To start with, use Warhammer Primarch worlds.

# Deployment

1. Deployment processes should be centred around automation.
2. If at all possible, build and deployment processes should use config as code.
3. All build processes should include any defined unit & integration tests.
