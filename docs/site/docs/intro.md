---
sidebar_position: 1
slug: /
---

# Introduction

Koala is a Kotlin JVM library for building and executing SQL.
It is designed to be a more powerful and complete alternative 
to the SQL DSL layer in ORMs like [Ktorm](https://github.com/kotlin-orm/ktorm)
and [Exposed](https://github.com/JetBrains/Exposed).
Koala is not an ORM and does not perform any Entity mapping.
Koala provides:

* Discoverable syntax that takes advantage of IDE auto-complete.
* Native support for advanced SQL like `WINDOW`, `WITH`, self-joins and more.
* An immutable and referentially transparent DSL.
* Predictable generation and execution of SQL. No N+1 or hidden laziness.
* Automatic schema migration for prototyping similar to
  [Prisma](https://www.prisma.io/)'s `db push`.
* Event interfaces for user provided logging. No hardcoded logging.
* Pure Kotlin dependencies with no code generation or plugins required.

## Purpose

Koala is intended for teams who don't need a full ORM and require more powerful
SQL generation than what Kotlin ORMs can currently provide.
Koala is designed to be self-contained, flexible and easy to integrate into existing projects.
By design it does not manage its own threading, connection pooling or logging
and delegates these concerns to the user.

## Supported databases

Koala currently supports the following databases:

| Database   | SQL DSL | Migration Support |
| ---------- | ------- | ----------------- |
| MySQL      | 🟩 Yes  | 🟩 Yes            |
| PostgreSQL | 🟩 Yes  | 🟨 Partial        |
| H2         | 🟩 Yes  | 🟥 No             |
