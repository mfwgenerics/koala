---
sidebar_position: 9
---

# Values clauses

## From rows

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val fakeShop = rowOf(
    ShopTable.name setTo "Fake Shop",
    ShopTable.address setTo "79 Fake Street, Fakesville"
)

val result = values(fakeShop)
    .perform(db)
    .single()

check("Fake Shop" == result[ShopTable.name])
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
VALUES ('Fake Shop', '79 Fake Street, Fakesville')
```

</TabItem>
</Tabs>
````

## From a collection

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

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
<TabItem value="SQL" label="SQL">

```sql
INSERT INTO "Customer"("name", "shop", "spent")
VALUES ('Dylan', 2, 10.80)
, ('Santiago', 2, 10.80)
, ('Chloe', 2, 10.80)
```

</TabItem>
</Tabs>
````

## Using labels

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

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

check(firstName == "SOFIA")
check(firstAge == 29)
```

</TabItem>
<TabItem value="SQL" label="SQL">

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
