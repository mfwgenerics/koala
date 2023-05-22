---
sidebar_position: 1
---

# Extending the DSL

In practice, it is necessary to extend Koala to support a larger set of SQL
and to better integrate with user-defined data types. Koala provides
different mechanisms to extend the library without having to revert to
raw queries.

## Mapped columns

Mapped columns are an easy way to support user defined types that can be expressed
in terms of existing column types.
This is useful for supporting wrapper types, enums and serialized data.

The code below establishes a user-defined `Email` data type and a corresponding `EMAIL`
column type by creating a mapping from the `VARCHAR(256)` column type.

```kotlin
data class Email(
    val asString: String
)

object CustomerTable : Table("Emails") {
    /*
    EMAIL will be treated as a VARCHAR(256) in generated SQL.
    Here we provide mappings between Email and the base type of String.
    */
    val EMAIL = VARCHAR(256).map(
        to = { string -> Email(string) },
        from = { email -> email.asString }
    )

    /* EMAIL can be treated like any other column type. Here we use it as a primary key. */
    val email = column("email", EMAIL.primaryKey())
    val name = column("name", VARCHAR(256))
}
```

Generated SQL will treat mapped column types the exact same way as their unmapped counterparts.

```sql title="SQL"
CREATE TABLE IF NOT EXISTS "Emails"(
"email" VARCHAR(256) NOT NULL,
"name" VARCHAR(256) NOT NULL,
CONSTRAINT "Emails_email_pkey" PRIMARY KEY ("email")
)
```

In the code below, we see our `Email` values are treated the same way as plain strings in generated SQL.

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
CustomerTable
    .insert(
        rowOf(
            CustomerTable.name setTo "Emanuel Smith",
            CustomerTable.email setTo Email("e.smith@example.com")
        )
    )
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
INSERT INTO "Emails"("name", "email")
VALUES ('Emanuel Smith', 'e.smith@example.com')
```

</TabItem>
</Tabs>
````

:::info

The examples on this page show generated SQL with raw values included. This is for display purposes.
Generated SQL in application code always uses parameterized queries to avoid SQL injection.

:::

### Enum columns

A common use case for mapped columns is storing and working with enums as strings.
Koala provides a method for easily creating enum mappings. Enum mappings can use any column
type as a base.

```kotlin
enum class TShirtEnum {
    XS, S, M, L, XL;
}

val TSHIRT_AS_VARCHAR = VARCHAR(256).mapToEnum<TShirtEnum> { tshirt ->
    tshirt.name
}

val TSHIRT_AS_INTEGER = INTEGER.mapToEnum<TShirtEnum> { tshirt ->
    tshirt.ordinal
}
```

:::caution

Storing enums as ints using `Enum.ordinal` can introduce backwards compatibility problems.

:::

## New column types

Koala doesn't natively support all the column types that exist across different SQL dialects.
We provide `JdbcExtendedDataType` to support column types that are not
included in the library.

The code below represents H2's UUID column type as a `java.util.UUID`.

```kotlin
val UUID_H2 = JdbcExtendedDataType(
    sql = "UUID", /* The raw SQL name of the column */
    jdbc = object : JdbcMappedType<UUID> { /* JDBC bindings for writing and reading UUIDs */
        override fun writeJdbc(stmt: PreparedStatement, index: Int, value: UUID) {
            stmt.setObject(index, value)
        }

        override fun readJdbc(rs: ResultSet, index: Int): UUID? =
            rs.getObject(index) as? UUID /* We need to handle the NULL case */
    }
)
```
