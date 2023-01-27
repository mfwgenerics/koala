---
title: Queries
sidebar_position: 4
---
## Selects
### Selecting all columns

Use `.selectAll()` to select all columns from a query.
````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val allSelected = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .selectAll()
    .perform(db)

assertContentEquals(ShopTable.columns, allSelected.columns)
```

</TabItem>
<TabItem value="sql" label="SQL">

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
<TabItem value="kotlin" label="Kotlin">

```kotlin
val implicitSelectAll = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .perform(db)

assertContentEquals(allSelected.columns, implicitSelectAll.columns)
```

</TabItem>
<TabItem value="sql" label="SQL">

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
### Individual columns

Selecting a small number of fixed columns gives you a specialized
query with statically typed rows of ordered columns.
You can use Kotlin's destructuring or call positional
methods to access these fields in a type safe way.
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val row: RowOfTwo<String, String> = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(ShopTable.name, ShopTable.address) // select a pair
    .perform(db)
    .single()

val (name, address) = row // destructuring support

assertEquals(
    name,
    row.first() // using positional methods also works
)

assertEquals(row[ShopTable.name], name)
assertEquals(row[ShopTable.address], address)

assertEquals("Helen's Hardware @ 63 Smith Street, Caledonia, 62281D", "$name @ $address")
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
SELECT T0."name" c0
, T0."address" c1
FROM "Shop" T0
WHERE T0."id" = 1
```

</TabItem>
</Tabs>
````
### All columns of a Table

Passing a `Table` to `.select` will automatically select all fields from that `Table`:
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val hardwareCustomers = ShopTable
    .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
    .where(ShopTable.id eq hardwareStoreId)
    .orderBy(CustomerTable.name)
    .select(CustomerTable) // select only fields from CustomerTable
    .perform(db)

assertContentEquals(CustomerTable.columns, hardwareCustomers.columns)
```

</TabItem>
<TabItem value="sql" label="SQL">

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
<TabItem value="kotlin" label="Kotlin">

```kotlin
ShopTable
    .innerJoin(CustomerTable, ShopTable.id eq CustomerTable.shop)
    .select(ShopTable, CustomerTable)
    .perform(db)
```

</TabItem>
<TabItem value="sql" label="SQL">

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
### Select distinct
All `.select(...)` methods have a corresponding `.selectDistinct(...)`.
```kotlin
val distinctShopIds = CustomerTable
    .selectDistinct(CustomerTable.shop)
    .perform(db)
    .map { it.first() }
    .toList()

assertContentEquals(distinctShopIds, distinctShopIds.distinct())
```
### Expressions and labels

Expressions can be selected by using `as_` to label them.
You can use an existing column name as a label or create an anonymous label:
```kotlin
val lowercaseName = label<String>()
```
Here we select `LOWER(ShopTable.name)` in SQL and label it:
```kotlin
val row = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(
        ShopTable,
        lower(ShopTable.name) as_ lowercaseName
    )
    .perform(db)
    .single()
```
The label can then be used to access the result of the expression:
```kotlin
assertEquals("helen's hardware", row[lowercaseName])
assertEquals(row[ShopTable.name].lowercase(), row[lowercaseName])
```
For convenience you can use the labeled expression to refer to the label.
Here is another way of writing the code from above:
```kotlin
val lowerName = lower(ShopTable.name) as_ label()

val row = ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .select(ShopTable, lowerName)
    .perform(db)
    .single()

assertEquals("helen's hardware", row[lowerName])
assertEquals(row[ShopTable.name].lowercase(), row[lowerName])
```
### Empty selections

Selects can be empty. This will generate SQL with `SELECT 1`
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val emptySelect = ShopTable
    .where(ShopTable.id inValues listOf(hardwareStoreId, groceryStoreId))
    .select()
    .perform(db)

assertEquals(0, emptySelect.columns.size)

val rowCount = emptySelect
    .count()

assertEquals(2, rowCount)
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
SELECT 1 c0
FROM "Shop" T0
WHERE T0."id" IN (1, 2)
```

</TabItem>
</Tabs>
````
### Expecting columns

If you know the exact columns a query will have at runtime,
you can convert it to a query of statically typed rows.
This is sometimes necessary when using subqueries in expressions.
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

assertEquals(
    genericRow[ShopTable.name],
    staticallyTypedRow.first()
)
```
This will not work if the columns sets do not match at runtime. The following code will fail:
```kotlin
assertFails {
    ShopTable
        .where(ShopTable.id eq hardwareStoreId)
        .select(listOf(ShopTable.address, ShopTable.name))
        .expecting(ShopTable.name)
}
```
## Joins
### Inner join
```kotlin
CustomerTable
    .innerJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)
```
### Left and right join
```kotlin
CustomerTable
    .leftJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)

CustomerTable
    .rightJoin(ShopTable, ShopTable.id eq CustomerTable.shop)
    .perform(db)
```
### Cross join
```kotlin
ShopTable
    .crossJoin(CustomerTable)
    .perform(db)
```
## Aliases
### Self-join with alias
```kotlin
val alias = alias()

val row = ShopTable
    .innerJoin(ShopTable.as_(alias), alias[ShopTable.id] eq groceryStoreId)
    .where(ShopTable.id eq hardwareStoreId)
    .perform(db)
    .single()

assertEquals("Helen's Hardware", row[ShopTable.name])
assertEquals("24 Hr Groceries", row[alias[ShopTable.name]])
```
## Where
### Chaining wheres
```kotlin
ShopTable
    .where(ShopTable.id eq hardwareStoreId)
    .where(ShopTable.name eq "Helen's Hardware")
    .perform(db)
    .single()
```
## Group By
### Group by
```kotlin
val customerCount = label<Int>()

CustomerTable
    .groupBy(CustomerTable.shop)
    .select(CustomerTable.shop, count() as_ customerCount)
    .perform(db)
```
## Windows
### Window clause
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val shopsWindow = window() as_ all().partitionBy(ShopTable.id)

val totalSpent = sum(CustomerTable.spent).over(shopsWindow) as_ label()
val rank = rank().over(shopsWindow.orderBy(CustomerTable.spent.desc())) as_ label()

val rankings = ShopTable
    .innerJoin(CustomerTable, CustomerTable.shop eq ShopTable.id)
    .window(shopsWindow)
    .orderBy(ShopTable.name, rank)
    .select(
        ShopTable.name,
        CustomerTable.name,
        CustomerTable.spent,
        rank,
        totalSpent
    )
    .perform(db)
    .map { row ->
        "${row[CustomerTable.name]} #${row[rank]} at ${row[ShopTable.name]} " +
        "spent $${row[CustomerTable.spent]} of $${row[totalSpent]}"
    }
    .toList()

assertContentEquals(
    rankings,
    listOf(
        "Angela Abara #1 at 24 Hr Groceries spent $79.99 of $79.99",
        "Michael M. Michael #1 at Helen's Hardware spent $125.00 of $145.50",
        "Maria Robinson #2 at Helen's Hardware spent $20.50 of $145.50"
    )
)
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
SELECT T0."name" c0
, T1."name" c1
, T1."spent" c2
, RANK() OVER (w0 ORDER BY T1."spent" DESC) c3
, SUM(T1."spent") OVER (w0) c4
FROM "Shop" T0
INNER JOIN "Customer" T1 ON T1."shop" = T0."id"
WINDOW w0 AS (PARTITION BY T0."id")
ORDER BY T0."name" ASC, c3 ASC
```

</TabItem>
</Tabs>
````
## Values
### From rows
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val fakeShop = rowOf(
    ShopTable.name setTo "Fake Shop",
    ShopTable.address setTo "79 Fake Street, Fakesville"
)

val result = values(fakeShop)
    .perform(db)
    .single()

assertEquals("Fake Shop", result[ShopTable.name])
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
VALUES ('Fake Shop', '79 Fake Street, Fakesville')
```

</TabItem>
</Tabs>
````
### From a collection
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val names = listOf("Dylan", "Santiago", "Chloe")

val customers = values(names) { name ->
    this[CustomerTable.name] = name
    this[CustomerTable.shop] = groceryStoreId
    this[CustomerTable.spent] = BigDecimal("10.80")
}

CustomerTable
    .insert(customers)
    .perform(db)
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
INSERT INTO "Customer"("name", "shop", "spent")
VALUES ('Dylan', 2, 10.80)
, ('Santiago', 2, 10.80)
, ('Chloe', 2, 10.80)
```

</TabItem>
</Tabs>
````
### Using labels
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
val name = label<String>("name")
val age = label<Int>("age")

val names = values(listOf("Jeremy", "Sofia")) {
    this[name] = it
    this[age] = 29
}

val (firstName, firstAge) = names
    .subquery()
    .orderBy(name.desc())
    .select(upper(name) as_ name, age)
    .perform(db)
    .first()

assertEquals(firstName, "SOFIA")
assertEquals(firstAge, 29)
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
SELECT UPPER(T0."name") "name"
, T0."age"
FROM (VALUES ('Jeremy', 29)
, ('Sofia', 29)) T0("name", "age")
ORDER BY T0."name" DESC
```

</TabItem>
</Tabs>
````
## Unions
## Order By
### Order by
```kotlin
val alphabetical = ShopTable
    .orderBy(ShopTable.name)
    .perform(db)

val reverseAlphabetical = ShopTable
    .orderBy(ShopTable.name.desc())
    .perform(db)
```
### Nulls first and last
```kotlin
ShopTable
    .orderBy(ShopTable.established.desc().nullsLast())
    .perform(db)

ShopTable
    .orderBy(ShopTable.established.asc().nullsFirst())
    .perform(db)
```
### Compound order
```kotlin
ShopTable
    .orderBy(
        ShopTable.established.desc().nullsLast(),
        ShopTable.name
    )
    .perform(db)
```
## Limits
### Limit
```kotlin
val firstByName = ShopTable
    .orderBy(ShopTable.name)
    .limit(1)
    .perform(db)
```
### Offset
```kotlin
val thirdByName = ShopTable
    .orderBy(ShopTable.name)
    .offset(2)
    .limit(1)
    .perform(db)
```
### Offset without limit
```kotlin
ShopTable
    .orderBy(ShopTable.name)
    .offset(2)
    .perform(db)
```
## Locking
### For update
````mdx-code-block

<Tabs>
<TabItem value="kotlin" label="Kotlin">

```kotlin
ShopTable
    .where(ShopTable.id.inValues(groceryStoreId, hardwareStoreId))
    .forUpdate()
    .perform(db)
```

</TabItem>
<TabItem value="sql" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
WHERE T0."id" IN (2, 1)
FOR UPDATE
```

</TabItem>
</Tabs>
````
## With