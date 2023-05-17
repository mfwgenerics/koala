---
sidebar_position: 6
---

# Ordering

## Order by

````mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
val alphabetical = ShopTable
    .orderBy(ShopTable.name)
    .perform(db)

val reverseAlphabetical = ShopTable
    .orderBy(ShopTable.name.desc())
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
ORDER BY T0."name" ASC

SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
ORDER BY T0."name" DESC
```

</TabItem>
</Tabs>
````

## Nulls first and last

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .orderBy(ShopTable.established.desc().nullsLast())
    .perform(db)

ShopTable
    .orderBy(ShopTable.established.asc().nullsFirst())
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
ORDER BY T0."established" DESC NULLS LAST

SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
ORDER BY T0."established" ASC NULLS FIRST
```

</TabItem>
</Tabs>
````

## Compound order

````mdx-code-block
<Tabs>
<TabItem value="Kotlin" label="Kotlin">

```kotlin
ShopTable
    .orderBy(
        ShopTable.established.desc().nullsLast(),
        ShopTable.name
    )
    .perform(db)
```

</TabItem>
<TabItem value="SQL" label="SQL">

```sql
SELECT T0."id" c0
, T0."name" c1
, T0."address" c2
, T0."established" c3
FROM "Shop" T0
ORDER BY T0."established" DESC NULLS LAST, T0."name" ASC
```

</TabItem>
</Tabs>
````
