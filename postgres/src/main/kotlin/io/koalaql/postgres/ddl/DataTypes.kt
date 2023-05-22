package io.koalaql.postgres.ddl

import io.koalaql.ddl.ExtendedDataType
import io.koalaql.ddl.JsonData
import kotlin.reflect.typeOf

/* We don't use JdbcExtendedDataType here because JDBC mappings are expected to already exist for JsonData */
object JSONB : ExtendedDataType<JsonData>(typeOf<JsonData>(), "JSONB")