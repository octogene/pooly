# Common Database module

## Database Migration Script Generation

This module provides a Gradle task to generate database migration scripts using Exposed.

### Generating Migration Scripts

The `generateMigrationScript` task is used to create new migration files.

#### Task

```bash
./gradlew generateMigrationScript
```

#### Dynamic Database Connection

To allow for dynamic connection to the database, you can provide the following properties when running the task:

*   `db.url`: The JDBC URL for your database.
*   `db.user`: The username for your database.
*   `db.password`: The password for your database.
*   `migration.name`: The name of the migration file.
*   `migration.tables`: A csv of table names to check for generating the migration script

These properties can be passed as Gradle system properties using the `-P` flag:

```bash
./gradlew generateMigrationScript -Pdb.url=<your_db_url> -Pdb.user=<your_db_user> -Pdb.password=<your_db_password> -Pmigration.name=<migration> -Pmigration.tables=<table names csv>
```

**Example:**

```bash
./gradlew generateMigrationScript -Pdb.url=jdbc:postgresql://localhost:5432/pooly_db -Pdb.user=pooly_user -Pdb.password=pooly_password -Pmigration.name=add roles -Pmigration.tables=users,prizes
```

### Migration Script Location

Generated migration scripts will be placed in the `src/main/resources/migrations` directory.
