Pre-release:
* Reorganize so table declarations don't depend on so many packages
* Don't require .select for exists/notExists
* Implement ON DUPLICATE/CONFLICT update for VALUES, SELECTS incl/ complex updates
* Add tests for defaults
* Emit CREATE TABLE INDEX as separate DDL in H2
* Better Postgres ON CONFLICT support - allow columns and WHERE to be conflict targets
* Prevent delete/update without where or other limiting condition
* Complete + test connection event logging interface
* Memory leak proofing, idempotent close, auto-close resources when connection closes
* Merge Cte and Alias - allow inner join to Alias OR Cte/CteAlias vs Alias distinction?
* Offset timezone tests for MySQL

Post-release
* RETURNING support
* onConflictIgnore compatibility flag for MySQL dialect that uses INSERT IGNORE
* NULL literal without using value(null)
* Insert ignore for mysql?
* Support CHECK
* Temporary table support
* Views support
* Better scope names? tag them with "outer", "inner" or alias perhaps
* Explain scope contents?