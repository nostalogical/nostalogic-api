# Database Styleguide

All **proper entities** should have a set of **standard columns**. A “proper” entity is a table that can function as a standalone piece of data, such a user or article, and not one used for linking purposes like a membership.

Standard columns for entities are:

- `id` - the unique ID of the record.
- `created` - timestamp the record was created at.
- `creator_id` - the ID of the user that created the record, or a placeholder if created by the system.
- `tenant` - name of the app the record is bound to.

**Each service uses its own schema** so that theoretically the schemas could be located in different databases. This means each schema should be treated as physically separate - setting a user ID as the creator of an article is fine, but foreign keys should not be used between schemas.

# Creation and Upgrade Scripts

Database population and updates are handled by SQL script files executed in order. These script files are located within the code, so any changes are covered by version control. The layout of the scripts is approximately like this, any may be altered to include any changes required:

```diff
|--/resources/
   |--/sql/
      |--/00_schema/
         |--001_<schema>_service_schema.sql
      |--/01_tables/
         |--101_<schema>_tables.sql
      |--/02_data/
         |--201_<schema>_defaults.sql
```

The scripts are divided into folders for readability, and both folders and script files are prefixed with a number to determine they order they should be run in, meaning the schema is created first, then tables, then data within tables.

The number of the script file name is also used for testing purposes, so different data can be injected into integration tests by overriding that specific file.

The following principles should be adhered to for database scripts:

- Scripts should be repeatable and non-destructive.
    - Running the scripts twice should not cause any changes the second time, for example creating a table should use `CREATE TABLE IF NOT EXISTS`, and inserts should use `ON CONFLICT DO NOTHING`.
- Foreign keys should be used where appropriate, but only within the same schema.
- Each schema should include all the standard tables.

# Standard Tables

There are several tables that should be present in each schema, mostly holding metadata related to the service or its history.

## Mod Entity

The `mod_entity` table is used to record modifications made to entities. This includes the user that made the alteration, the time it happened, entity ID, original and new values. Entries to this table must be made whenever an entity is modified.

Tracking changes this way means the columns being altered on any update need to be known. Modifications should also support a **cluster ID** so if multiple changes are made at once they can all be linked to make searching histories easier.

The modification history should have a **primary and secondary entity**, so for example adding a user to a group could be found by searching for either the group or user ID in either column.

## Service Config

The `service_config` table is used to store dynamic configuration for each service, along with each setting’s source.

## Schema History

The `schema_history` table is used to record the history of the schema itself in its current database. This includes the timestamp of the initial schema creation, and the details of any subsequent updates.

To avoid deleting any history data, all updates should include a type and a rollback time, so similar to git reverting any change is handled as an update in the history.

History types are:

- `STARTUP`
    - This records each time the server is started - and therefore that the startup scripts are run - whether or not that result in any changes.
    - Each time
- `MIGRATION`
    - Added when a specific migration process is performed, so there could be 0, 1, or several added per startup.
    - These will generally run within an IF statement which checks that the migration, without a rollback, does not already exist in the schema history before performing it, or that the server is currently below a specific version.
- `ROLLBACK`
    - A rollback is a manually run reversion of changes done by one or more updates.
    - The rollback itself is a single entry in the schema history, but when it’s inserted it should be done alongside setting the rollback time on any previous updates this affects.

If an update is rolled back then a rollback time is set on the original update, so any searches for it as an existing update can include a check for the rollback being null.

### Version Format

Database version changes should include a version number. This starts with the date, in y-m-d format, followed by a 3 digit number, e.g. 2022-01-04-001

## Database Migrations

Database migrations, such as table changes or data changes, are often needed as part of server updates. If these are changes they should always be included as scripts in a unique update script folder.

Both fresh **server creations** and **existing server updates** should be covered by migration scripts. This will often mean that the easiest solution to a performing a migration is to just create a new script to handle the tasks after the initial base scripts have run, but this can lead to the growth of an unwieldly and hard to read set of migration scripts after ideally the more orderly initial database creation scripts. Therefore if possible migrations should also be included in the initial creation scripts, such as adding a new column to a table or changing a column type.

In all circumstances database migrations should include a **comment** stating whether or not the change is also present in the creation scripts, and some indication of when it can be deprecated - the point when it’s unlikely to need to be automatically run again.

Once **migration scripts** are no longer needed and can be rolled into the startup scripts, they should be **retired**. This is done by moving them from the numbered folders that are automatically executed to an archived migration folder, or they can just be deleted if having them available for reference is unnecessary - they should still be available within version control.

There are times when **rolling back** an update is required, such as when moving to an older branch with a newer database. This will generally always require an ad-hoc rollback script to be created if the schema contains breaking changes, so these should be stored in a rollback folder to allow them to be reused if needed.

# Rolling Upgrades

Rolling upgrades are not currently something that needs to be supported so it can be ignored for now.

If this is ever required then certain principles will need to be adhered to, for example:

- Not deleting or renaming existing columns in the initial release.
- Continuing to write to deprecated columns while still in use.

It may also be useful to apply some form of versioning to both entities and requests.
