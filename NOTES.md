Project consists of two parts - schema and query:

Schema definition and diff framework:

* Aims to faithfully represent schema rather than DDL
* Does not provide any DDL directly
* Instead, DDL is produced from changesets

SQL query/statement dsl:

* Aims to faithfully represent SQL
* Should be clear what SQL is generated
* 1-1 correspondence between DSL queries and SQL

Query and statement DSLs should be syntactically close to SQL:
* DSL elements should be mostly from their corresponding SQL (e.g. SELECT .select(), IS NOT NULL .isNotNull())
* Order and arity should approximately match SQL (e.g. ORDER BY x DESC becomes orderBy(x.desc()) rather than orderBy(desc(x))) 
* Generated SQL is explicit and easily predicted from query DSL

Loosely coupled design:
* Interfaces between layers
* Substitutability of interfaces outside of ADT sealed classes
* No details of dialect or JDBC leak back into DSL layer
* It should be possible to isolate and reuse the DSL layer entirely, separately from the rest of the project  

Complete and legible DSL interface
* Simple use cases should form subsets of complex ones e.g. ORDERING BY one field should use the same method as ordering by multiple fields or DESC
* Use cases should not require breaking out of the DSL interface and directly instantiating classes
