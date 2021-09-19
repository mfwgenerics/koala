* Decide whether to use typeof despite experimental status
* Reorganize so table declarations don't depend on so many packages
* Add a null expression so users don't have to use value(null)
* Don't require .select for exists/notExists
* Test to ensure that Postgres ON CONFLICT DO UPDATE works with tables named EXCLUDED
* Implement ON DUPLICATE/CONFLICT update for VALUES, SELECTS incl/ complex updates
* Add tests for defaults
* Support CHECK
* Emit CREATE TABLE INDEX as separate DDL in H2
* onConflictIgnore compatibility flag for MySQL dialect that uses INSERT IGNORE
* Better Postgres ON CONFLICT support - allow columns and WHERE to be conflict targets
* Insert ignore for mysql?
* Temporary table support
* Views support
* Correct way for implementations to check dialect?
* Prevent delete/update without where or other limiting condition
* Complete + test connection event logging interface
* Memory leak proofing, idempotent close, auto-close resources when connection closes
* RETURNING support and change existing .returning to be about auto-generated keys only
* Merge Cte and Alias - allow inner join to Alias OR Cte/CteAlias vs Alias distinction?
* Error case for empty VALUES
* Offset timezone tests for MySQL 