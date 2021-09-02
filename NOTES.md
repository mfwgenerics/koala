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
 
* Share test code
* Figure out cleanup for mysql
* throw exception on UPDATE/DELETE with no where clause?
* SELECT optional for subUNION (provided by outer query)
* backtick escape names and prevent auto-generated name collision
* do not attempt to represent NULLability as Kotlin optional. poor fit to SQL semantics
* shared subcase for different INT data types?

Selection:
* Need a way to dynamically select (select only fields that query brings into scope)
* A selectAll type option?
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
* Sort out huge mess around quoting/non-quoting identifiers and aliases:
   1. user provided names should be case-sensitive and always quoted.
   2. generated names chosen such that they don't require case sensitivity or quoting
   3. Use type wrappers to differentiate between the different cases?
   4. identifiers should be whitelisted or marked for escaping?  