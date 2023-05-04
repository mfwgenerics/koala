---
sidebar_position: 1
---

# Selects

## Selecting all columns

Use `.selectAll()` to select all columns from a query.

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val allSelected = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .selectAll()
    .perform(db)

check(ShopTable.columns.deepEquals(allSelected.columns))
check("Helen's Hardware" == allSelected.first()[ShopTable.name])
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

In most cases `.selectAll()` can be omitted:

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val implicitSelectAll = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .perform(db)

check(ShopTable.columns.deepEquals(implicitSelectAll.columns))
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

:::info

`SELECT * FROM` will never appear in generated SQL.
The generated SQL for these queries will name columns explicitly.

:::

## Individual columns

Selecting a small number of fixed columns gives you a specialized
query with statically typed rows of ordered columns.
You can use Kotlin's destructuring or call positional
methods to access these fields in a type safe way.

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val row: RowOfTwo<String, String> = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(ShopTable.name, ShopTable.address) // select a pair
    .perform(db)
    .single()

val (name, address) = row // destructuring support

// using positional methods also works
check(name == row.first())

check(row[ShopTable.name] == name)
check(row[ShopTable.address] == address)

check("Helen's Hardware @ 63 Smith Street, Caledonia, 62281D" == "$name @ $address")
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."name" c0
, T0."address" c1
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

## All columns of a Table

Passing a `Table` to `.select` will automatically select all fields from that `Table`:

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val hardwareCustomers = ShopTable
    .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
    .where(ShopTable.id eq hardwareStoreId)
    .orderBy(CustomerTable.name)
    .select(CustomerTable) // select only fields from CustomerTable
    .perform(db)

check(CustomerTable.columns.deepEquals(hardwareCustomers.columns))
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."shop" c1
, T0."name" c2
, T0."spent" c3
FROM "Shop" T1
INNER JOIN "Customer" T0 ON T1."id" = T0."shop"
WHERE T1."id" = 1
ORDER BY T0."name" ASC
```

</TabItem>
</Tabs>
````

You can pass multiple `Table`s to select:

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
    .select(ShopTable, CustomerTable)
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
, T1."id" c4
, T1."shop" c5
, T1."name" c6
, T1."spent" c7
FROM "Shop" T0
INNER JOIN "Customer" T1 ON T0."id" = T1."shop"
```

</TabItem>
</Tabs>
````

## Select distinct

All `.select(...)` methods have a corresponding `.selectDistinct(...)`.

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val distinctShopIds = CustomerTable
    .selectDistinct(CustomerTable.shop)
    .perform(db)
    .map { it.first() }
    .toList()

check(distinctShopIds.deepEquals(distinctShopIds.distinct()))
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT DISTINCT T0."shop" c0
FROM "Customer" T0
```

</TabItem>
</Tabs>
````

## Expressions and labels

Expressions can be selected by using `as_` to label them.
You can use an existing column name as a label or create an anonymous label:

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val lowercaseName = label<String>()

/*
Here we select `LOWER(ShopTable.name)` in SQL and label it:
*/

val row = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(
        ShopTable,
        lower(ShopTable.name) as_ lowercaseName
    )
    .perform(db)
    .single()

/*
The label can then be used to access the result of the expression:
 */

check("helen's hardware" == row[lowercaseName])
check(row[ShopTable.name].lowercase() == row[lowercaseName])
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
, LOWER(T0."name") c4
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

For convenience you can use the labeled expression to refer to the label.
Here is another way of writing the code from above:

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val lowerName = lower(ShopTable.name) as_ label()

val row = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(ShopTable, lowerName)
    .perform(db)
    .single()

check("helen's hardware" == row[lowerName])
check(row[ShopTable.name].lowercase() == row[lowerName])
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
, LOWER(T0."name") c4
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

## Empty selections

Selects can be empty. This will generate SQL with `SELECT 1`

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val emptySelect = ShopTable
    .where(ShopTable.id inValues listOf(hardwareStoreId, groceryStoreId))
    .select()
    .perform(db)

check(emptySelect.columns.isEmpty())

val rowCount = emptySelect
    .count()

check(2 == rowCount)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT 1 c0
FROM "Shop" T0
WHERE T0."id" IN (1, 2)
```

</TabItem>
</Tabs>
````

## Expecting columns

If you know the exact columns a query will have at runtime,
you can convert it to a query of statically typed rows.
This is sometimes necessary when using subqueries in expressions.

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val columnsList = listOf(ShopTable.address, ShopTable.name)

val query = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(columnsList)

val genericRow: ResultRow = query
    .perform(db)
    .single()

val staticallyTypedRow: RowOfTwo<String, String> = query
    .expecting(ShopTable.name, ShopTable.address) // convert to statically typed query
    .perform(db)
    .single()

check(genericRow[ShopTable.name] == staticallyTypedRow.first())
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."address" c0
, T0."name" c1
FROM "Shop" T0
WHERE T0."id" = 1

SELECT T0."name" c0
, T0."address" c1
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````

This will not work if the columns sets do not match at runtime. The following code will fail:

```kotlin
ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(listOf(ShopTable.address, ShopTable.name))
    .expecting(ShopTable.name)
```
