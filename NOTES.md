Project consists of two parts - schema and query:

Schema definition and diff framework:

* Aims to faithfully represent schema
* Does not provide DDL directly
* Instead, DDL is figured out from diffs 

SQL query/statement dsl:

* Aims to faithfully represent SQL
* Should be clear what SQL is generated
* 1-1 correspondence between DSL queries and SQL

Misc:
 
* throw exception on UPDATE/DELETE with no where clause?
* SELECT optional for subUNION (provided by outer query)
* Support VALUES (how to bind value colums to fields?)
* Correlated subquery support
* backtick escape names and prevent auto-generated name collision
* do not attempt to represent NULLability as Kotlin optional. poor fit to SQL semantics
* shared subcase for different INT data types?
* add some selectAll type option?