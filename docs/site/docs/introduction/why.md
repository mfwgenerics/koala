---
sidebar_position: 2
---

# Why Koala

Koala is for Kotlin developers who desire a lean, type-safe
library for producing SQL without having to sacrifice modern SQL features.
Abstractions provided by the DSL are limited to convenience and quality of life
features. SQL generation is direct and predictable.

In addition to SQL generation, Koala includes automatic schema migration
tooling that allows you to forego traditional database migrations during development.
At initialization, Koala can compare your table definitions to your database
and emit DDL to modify the database.
This allows you to add, remove and modify columns and keys on your tables
without writing migrations.

## What Koala isn't

### An ORM

Koala is not an ORM. It is only concerned with SQL and your database.
It does not attempt to manage entity mappings or bind table rows to classes.
It doesn't perform any caching or automatic joining. It is a type-safe library
for generating and executing SQL.

### A universal SQL dialect

Every database has a slightly different dialect of SQL. Koala abstracts over some differences
such as tiny variations in syntax, keywords, datatype and operator names.
However, SQL that is functionally specific to a dialect will be generated as is.
For example, `.insertIgnore()` will generate `INSERT IGNORE` regardless of database.

### A completed product

Koala and its documentation are still under active development.
Although the design is complete, many of the individual functions
and operations from SQL are missing. There are also rough edges and
quality of life issues.
We encourage new users to open GitHub issues documenting any problems they encounter
or feature requests they have.

## Benefits of Koala

### Discoverable syntax

Koala is designed to reflect the syntax of SQL in a predictable fashion.
Methods and functions are named according to their underlying SQL.
Clauses are ordered according to their underlying SQL with the exception of `select`.
Interfaces and classes are designed to work with IDE auto-complete and to avoid
polluting auto-completion with non-SQL methods.

### Designed for modern SQL

Koala is designed to support modern SQL features in a unified syntax.
You can easily write complex SQL involving window functions, CTEs,
sub-selects, correlated queries, self-joins, unions, labels and value clauses
without dropping out of the DSL.
Complex queries can be written using the same APIs as simpler queries.

### Immutable, value-based DSL

Koala DSL expressions are immutable and can be shared and re-used easily.
They are regular Kotlin expressions that can be assigned to variables, passed
into methods and shared between threads. Koala does not use global state.

### Explicit behavior

Queries and statements in Koala will never execute more than
one query against the underlying database.
Side effects are predictable and are isolated to specific `perform` method calls.
Koala is thread agnostic and does not use thread local state.

### Pure Kotlin

Koala only depends on the Kotlin standard library and the Postgres JDBC driver for
`koala-postgres`.
Logging is provided through a structured logging interface.
You have complete control over the emission, destination and format of logs.
Koala does not require configuration files, environment variables.
No plugins, code generation or extra build steps are required to use Koala effectively.
