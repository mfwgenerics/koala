---
sidebar_position: 1
---

# Update

## Empty Updates

```kotlin
val updated = ShopTable
    .update()
    .perform(db)

check(0 == updated)
```
