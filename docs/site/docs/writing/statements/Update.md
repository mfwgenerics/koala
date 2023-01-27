---
title: Updates
sidebar_position: 1
---
### Empty updates
```kotlin
val updated = ShopTable
    .update()
    .perform(db)

assertEquals(0, updated)
```