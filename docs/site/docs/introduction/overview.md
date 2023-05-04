---
sidebar_position: 1
slug: /
---

# Overview

Koala is a Kotlin JVM library for building and executing SQL.
It is designed to be a more powerful and complete alternative 
to the SQL DSL layer in ORMs like [Ktorm](https://github.com/kotlin-orm/ktorm)
and [Exposed](https://github.com/JetBrains/Exposed).
Koala is not an ORM and does not perform any Entity mapping.

You can learn more about Koala in our Slack channel [#koalaql](https://kotlinlang.slack.com/archives/C04PT610JRK)
on the Kotlin Slack.

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
