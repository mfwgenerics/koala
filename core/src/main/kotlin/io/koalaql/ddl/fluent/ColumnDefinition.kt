package io.koalaql.ddl.fluent

import io.koalaql.ddl.built.BuildsIntoColumnDef

interface ColumnDefinition<T : Any>: BuildsIntoColumnDef