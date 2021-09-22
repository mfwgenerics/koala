Pre-release:
* Reorganize so table declarations don't depend on so many packages
* Implement ON DUPLICATE/CONFLICT update for VALUES, SELECTS incl/ complex updates
* Add tests for defaults
* Emit CREATE TABLE INDEX as separate DDL in H2
* Better Postgres ON CONFLICT support - allow columns and WHERE to be conflict targets
* Prevent delete/update without where or other limiting condition
* Complete + test connection event logging interface
* Memory leak proofing, idempotent close, auto-close resources when connection closes
* Merge Cte and Alias - allow inner join to Alias OR Cte/CteAlias vs Alias distinction?
* Offset timezone tests for MySQL
* Well defined defaults for collation / charset
* Add table definitions + null cases to DataTypeTests
* Large text blocks test
* VARCHAR maxLength tests
* Common operators w/ tests for MySQL
* Replace chained .where with e.g. .where().and().and()

?-release:
* User specified collations and charsets

Post-release
* Don't require .select for exists/notExists
* RETURNING support
* onConflictIgnore compatibility flag for MySQL dialect that uses INSERT IGNORE
* NULL literal without using value(null)
* Insert ignore for mysql?
* Support CHECK
* Temporary table support
* Views support
* Better scope names? tag them with "outer", "inner" or alias perhaps
* Explain scope contents?
* Better generic support / rely less on reified in interfaces
* Fix the hacky testing situation (test artifact jar / empty test method workaround)
* JSON support