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


Pain points:
* No way to dynamically select (select only fields that query brings into scope)
* need some selectAll type option?
* Selecting a whole Table should only select the table fields in scope? error is surprising to user  

Data typing:
 
* compiler/dialect builds up KClass<*> -> DataType map?
* canonical deserializations for inbuilt DataType's canonical KClass<*>s?

Error handling:

* Wrap exceptions so the issue can be pointed to in the generated SQL
* Better scope names? tag them with "outer", "inner" or alias perhaps
* Explain scope contents?

Refactoring:
* Clean up spaghetti of update/insert/select distinctions
* Deep nested field accesses in H2 compiler