package io.koalaql.query.fluent

import io.koalaql.query.Queryable

interface WithableQueryable<out T>: Queryable<T>, Withable<Queryable<T>>