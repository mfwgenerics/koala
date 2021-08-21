Query and statement DSLs should be syntactically close to SQL:
* DSL elements should be predictable from their corresponding SQL (e.g. SELECT .select(), IS NOT NULL .isNotNull())
* Order and arity should closely match SQL (e.g. ORDER BY x DESC becomes orderBy(x.desc()) rather than orderBy(desc(x))) 
* Exceptions exist for things like .select (end of query) and .with (after table)
* Generated SQL is explicit and easily predicted from query DSL

DSLs should aim to support many modern SQL features:
* CTEs
* WINDOW functions
* Features that aren't supported should be given space for future design

Loosely coupled design:
* Interfaces between layers
* Respect Liskov substitutability outside of ADT sealed classes
* DSL classes should not depend on specific details from dialect or transaction layer
* It should be possible to isolate and reuse the DSL layer entirely, separately from the rest of the project  

Complete and legible DSL interface
* Simple use cases should form subsets of complex ones e.g. ORDERING BY one field should use the same method as ordering by multiple fields or DESC
* Use cases should not require breaking out of the DSL interface and directly constructing ADTs
