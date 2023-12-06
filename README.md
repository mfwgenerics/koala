# Koala

Koala is an SQL DSL for Kotlin.

# Usage documentation

User docs are under construction [here](https://mfwgenerics.github.io/koala/)

# Developing

Import the project directory into IntelliJ IDEA as a Gradle project.

## Running tests

Tests require PostgreSQL and MySQL running in Docker. Start the database instances
by running [postgres/setup.sh](postgres/setup.sh) and [mysql/setup.sh](mysql/setup.sh).

Tests should now run successfully:

```sh
./gradlew test
```

## Running the user documentation

To interactively develop the user documentation, run the following task:

```sh
./gradlew :docs:docusaurusStart --continuous
```

This will serve documentation on `http://localhost:3000/koala` by default.

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)
