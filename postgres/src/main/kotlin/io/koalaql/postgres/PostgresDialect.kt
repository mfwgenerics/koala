package io.koalaql.postgres

import io.koalaql.Assignment
import io.koalaql.ddl.*
import io.koalaql.ddl.built.BuiltIndexDef
import io.koalaql.ddl.built.ColumnDefaultExpr
import io.koalaql.ddl.built.ColumnDefaultValue
import io.koalaql.ddl.diff.SchemaChange
import io.koalaql.dialect.*
import io.koalaql.expr.*
import io.koalaql.expr.built.BuiltAggregatable
import io.koalaql.query.*
import io.koalaql.query.built.*
import io.koalaql.sql.*
import io.koalaql.window.*
import io.koalaql.window.built.BuiltWindow
import kotlin.reflect.KType

private fun UnmappedDataType<*>.toRawSql(): String = when (this) {
    DOUBLE -> "DOUBLE PRECISION"
    FLOAT -> "REAL" /* "FLOAT" type in postgres is double precision/float8 */
    else -> defaultRawSql()
}

class PostgresDialect: SqlDialect {
    private val compiler = object : Compiler {
        override fun excluded(builder: ScopedSqlBuilder, reference: Reference<*>) {
            builder.addSql("EXCLUDED.")

            when (reference) {
                is Column<*> -> builder.addIdentifier(reference.symbol)
                else -> builder.compileReference(reference)
            }
        }

        override fun <T : Any> reference(builder: ScopedSqlBuilder, emitParens: Boolean, value: Reference<T>) {
            builder.compileReference(value)
        }

        override fun subquery(builder: ScopedSqlBuilder, emitParens: Boolean, subquery: BuiltSubquery) {
            builder.compileSubqueryExpr(subquery)
        }

        override fun aggregatable(builder: ScopedSqlBuilder, emitParens: Boolean, aggregatable: BuiltAggregatable) {
            builder.compileAggregatable(aggregatable)
        }

        override fun <T : Any> dataTypeForCast(builder: ScopedSqlBuilder, to: UnmappedDataType<T>) {
            builder.compileCastDataType(to)
        }

        override fun window(builder: ScopedSqlBuilder, window: BuiltWindow) {
            builder.compileWindow(window)
        }

        override fun compileExpr(
            builder: ScopedSqlBuilder,
            expr: QuasiExpr,
            emitParens: Boolean,
            emitAliases: Boolean
        ) {
            when {
                expr is OperationExpr<*> && expr.type == StandardOperationType.CURRENT_TIMESTAMP -> {
                    check(expr.args.isEmpty())

                    builder.parenthesize(emitParens) {
                        builder.addSql("NOW()")
                    }
                }
                else -> super.compileExpr(builder, expr, emitParens, emitAliases)
            }
        }
    }

    private fun ScopedSqlBuilder.compileDefaultExpr(expr: Expr<*>) {
        when (expr) {
            is Literal -> compiler.addLiteral(this, expr)
            is Column<*> -> addIdentifier(expr.symbol)
            else -> error("not implemented")
        }
    }

    private fun ScopedSqlBuilder.compileDataType(type: UnmappedDataType<*>) {
        addSql(type.toRawSql())
    }

    private fun ScopedSqlBuilder.compileSerialType(type: UnmappedDataType<*>) {
        when (type) {
            SMALLINT -> addSql("SMALLSERIAL")
            INTEGER -> addSql("SERIAL")
            BIGINT -> addSql("BIGSERIAL")
            else -> addError("no serial type corresponds to $type")
        }
    }

    private fun ScopedSqlBuilder.compileIndexConstraint(name: String, def: BuiltIndexDef) {
        addSql("CONSTRAINT ")

        addIdentifier(name)

        addSql(when (def.type) {
            IndexType.PRIMARY -> " PRIMARY KEY"
            IndexType.UNIQUE -> " UNIQUE"
            IndexType.INDEX -> " INDEX"
        })

        addSql(" ")
        parenthesize {
            prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDefaultExpr(key)
            }
        }
    }

    private fun ScopedSqlBuilder.compileIndexDef(table: Table, name: String, def: BuiltIndexDef) {
        addSql("CREATE INDEX IF NOT EXISTS ")

        addIdentifier(name)

        addSql(" ON ")

        addTableReference(table)

        addSql(" ")
        parenthesize {
            prefix("", ", ").forEach(def.keys.keys) { key ->
                compileDefaultExpr(key)
            }
        }
    }

    private fun ScopedSqlBuilder.compileColumnType(column: TableColumn<*>) {
        val def = column.builtDef

        if (def.autoIncrement) {
            compileSerialType(def.columnType.dataType)
        } else {
            compileDataType(def.columnType.dataType)
        }
    }

    private fun ScopedSqlBuilder.compileColumnDefault(column: TableColumn<*>) {
        val def = column.builtDef

        def.default?.let { default ->
            val finalExpr = when (default) {
                is ColumnDefaultExpr -> default.expr
                is ColumnDefaultValue -> Literal(
                    def.columnType.type,
                    default.value
                )
            }

            addSql(" DEFAULT ")
            compileDefaultExpr(finalExpr)
        }
    }

    private fun ScopedSqlBuilder.compileColumnDef(column: TableColumn<*>) {
        addIdentifier(column.symbol)
        addSql(" ")

        compileColumnType(column)

        val def = column.builtDef

        if (def.notNull) addSql(" NOT NULL")

        compileColumnDefault(column)
    }

    private fun ScopedSqlBuilder.compileCreateTable(table: Table) {
        addSql("CREATE TABLE IF NOT EXISTS ")

        addTableReference(table)
        parenthesize {
            val comma = prefix("\n", ",\n")

            comma.forEach(table.columns.includingUnused()) {
                compileColumnDef(it)
            }

            table.primaryKey?.let { pk ->
                comma.next {
                    addSql("CONSTRAINT ")
                    addIdentifier(pk.name)
                    addSql(" PRIMARY KEY (")
                    prefix("", ", ").forEach(pk.def.keys.keys) {
                        when (it) {
                            is TableColumn<*> -> addIdentifier(it.symbol)
                            else -> error("expression keys unsupported")
                        }
                    }
                    addSql(")")
                }
            }

            table.indexes.forEach { index ->
                if (index.def.type == IndexType.UNIQUE) comma.next {
                    compileIndexConstraint(index.name, index.def)
                }
            }

            addSql("\n")
        }
    }

    private fun ScopedSqlBuilder.compileAlterColumnType(
        table: Table,
        column: TableColumn<*>
    ) {
        val cname = column.symbol

        addSql("ALTER TABLE ")
        addTableReference(table)
        addSql(" ALTER COLUMN ")
        addIdentifier(cname)

        addSql(" TYPE ")

        compileColumnType(column)

        val using = column.builtDef.using

        if (using != null) {
            val usingExpr = using(RawExpr<Any> { identifier(cname) })

            addSql(" USING ")
            compileDefaultExpr(usingExpr)
        }
    }

    private fun ScopedSqlBuilder.compileAlterColumnDefault(
        table: Table,
        column: TableColumn<*>
    ) {
        addSql("ALTER TABLE ")
        addTableReference(table)
        addSql(" ALTER COLUMN ")
        addIdentifier(column.symbol)

        val newDefault = column.builtDef.default

        if (newDefault == null) {
            addSql(" DROP DEFAULT")
        } else {
            addSql(" SET")
            compileColumnDefault(column)
        }
    }

    override fun ddl(change: SchemaChange): List<CompiledSql> {
        val results = mutableListOf<(ScopedSqlBuilder) -> Unit>()

        change.tables.created.forEach { (_, table) ->
            results.add { sql ->
                sql.compileCreateTable(table)
            }
        }

        change.tables.created.forEach { (_, table) ->
            table.indexes.forEach { index ->
                if (index.def.type == IndexType.INDEX) {
                    results.add { sql ->
                        sql.compileIndexDef(table, index.name, index.def)
                    }
                }
            }
        }

        change.tables.altered.forEach { (_, table) ->
            table.columns.created.forEach { (_, column) ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addTableReference(table.newTable)
                    sql.addSql(" ADD COLUMN ")

                    sql.compileColumnDef(column)
                }
            }

            table.columns.altered.forEach { (_, column) ->
                if (column.type != null) results.add { it.compileAlterColumnType(table.newTable, column.newColumn) }
                if (column.changedDefault != null) results.add { it.compileAlterColumnDefault(table.newTable, column.newColumn) }
            }

            table.columns.dropped.forEach { column ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addTableReference(table.newTable)
                    sql.addSql(" DROP COLUMN ")
                    sql.addIdentifier(column)
                }
            }

            table.indexes.created.forEach { (name, index) ->
                when (index.type) {
                    IndexType.UNIQUE -> results.add { sql ->
                        sql.addSql("ALTER TABLE")
                        sql.addTableReference(table.newTable)
                        sql.addSql(" ADD CONSTRAINT ")
                        sql.addIdentifier(name)
                        sql.addSql(" UNIQUE ")

                        sql.parenthesize {
                            sql.prefix("", ", ").forEach(index.keys.keys) { key ->
                                sql.compileDefaultExpr(key)
                            }
                        }
                    }
                    IndexType.INDEX -> results.add { sql ->
                        sql.compileIndexDef(table.newTable, name, index)
                    }
                    else -> { }
                }
            }

            table.indexes.altered.forEach {
                error("Altering an existing index/key is not supported")
            }

            table.indexes.dropped.forEach { name ->
                results.add { sql ->
                    sql.addSql("ALTER TABLE ")
                    sql.addTableReference(table.newTable)
                    sql.addSql(" DROP CONSTRAINT IF EXISTS ")
                    sql.addIdentifier(name)
                }

                results.add { sql ->
                    sql.addSql("DROP INDEX IF EXISTS ")
                    sql.addIdentifier(name)
                }
            }
        }

        return results.map {
            ScopedSqlBuilder(
                CompiledSqlBuilder(PostgresDdlEscapes),
                Scope(NameRegistry { "column${it + 1}" }),
                compiler
            ).also(it).toSql()
        }
    }


    fun ScopedSqlBuilder.compileReference(name: Reference<*>) {
        resolveReference(name)
    }

    fun ScopedSqlBuilder.compileOrderBy(ordinals: List<Ordinal<*>>) {
        compileOrderBy(ordinals) {
            compileExpr(it, false)
        }
    }

    fun ScopedSqlBuilder.compileAggregatable(aggregatable: BuiltAggregatable) {
        if (aggregatable.distinct == Distinctness.DISTINCT) addSql("DISTINCT ")

        compileExpr(aggregatable.expr, false)

        if (aggregatable.orderBy.isNotEmpty()) compileOrderBy(aggregatable.orderBy)
    }

    fun ScopedSqlBuilder.compileWindow(window: BuiltWindow) =
        compileWindow(window,
            compileExpr = { compileExpr(it) },
            compileOrderBy = { compileOrderBy(it) }
        )

    fun ScopedSqlBuilder.compileCastDataType(type: UnmappedDataType<*>) {
        addSql(type.toRawSql())
    }

    fun ScopedSqlBuilder.compileFullQuery(query: BuiltQuery): Boolean {
        return scopedCtesIn(query) {
            compileFullQuery(
                query = query,
                compileWiths = { compileWiths(it) },
                compileSubquery = { compileQuery(it) },
                compileOrderBy = {
                    scopedIn(query) {
                        compileOrderBy(it)
                    }
                }
            )
        }
    }

    fun ScopedSqlBuilder.compileStmt(stmt: BuiltStatement): Boolean =
        when (stmt) {
            is BuiltInsert -> { compileInsert(stmt) }
            is BuiltUpdate -> { compileUpdate(stmt) }
            is BuiltDelete -> {
                compileDelete(stmt)
                true
            }
        }

    fun ScopedSqlBuilder.compileQuery(query: BuiltSubquery): Boolean =
        when (query) {
            is BuiltQuery -> compileFullQuery(query)
            is BuiltReturning -> compileReturning(query,
                compileStmt = { compileStmt(it) },
                compileExpr = { compileExpr(it, false) }
            )
        }

    fun ScopedSqlBuilder.compileQuery(query: BuiltUnionOperandQuery): Boolean {
        val compilation = withScope(query)

        return when (query) {
            is BuiltSelectQuery -> {
                compilation.compileSelect(query)
                return true
            }
            is BuiltValuesQuery -> compilation.compileValues(query)
        }
    }

    fun ScopedSqlBuilder.compileSubqueryExpr(subquery: BuiltSubquery) {
        parenthesize {
            compileQuery(subquery)
        }
    }

    fun ScopedSqlBuilder.compileSetLhs(expr: Reference<*>) {
        resolveWithoutAlias(expr)
    }

    fun ScopedSqlBuilder.compileExpr(
        expr: QuasiExpr,
        emitParens: Boolean = true,
        emitAliases: Boolean = true,
    ) {
        compiler.compileExpr(
            this,
            expr,
            emitParens,
            emitAliases
        )
    }

    fun ScopedSqlBuilder.compileRelation(relation: BuiltRelation) {
        val explicitLabels = when (val baseRelation = relation.relation) {
            is TableRelation -> {
                addTableReference(baseRelation)
                null
            }
            is Subquery -> {
                parenthesize {
                    compileQuery(baseRelation.of)
                }

                if (baseRelation.of.columnsUnnamed()) {
                    baseRelation.of.columns
                } else {
                    null
                }
            }
            is Cte -> {
                addCte(baseRelation)

                if (relation.computedAlias.identifier == baseRelation.identifier) return

                null
            }
            is EmptyRelation -> return
        }

        addSql(" ")
        addAlias(relation.computedAlias)

        explicitLabels?.let { labels ->
            parenthesize {
                prefix("", ", ").forEach(labels) {
                    addReference(it)
                }
            }
        }
    }

    fun ScopedSqlBuilder.compileRelabels(labels: List<Reference<*>>) {
        parenthesize {
            prefix("", ", ").forEach(labels) {
                addReference(it)
            }
        }
    }

    fun ScopedSqlBuilder.compileWiths(withable: BuiltWithable) = compileWiths(
        withable,
        compileCte = { addCte(it) },
        compileRelabels = { compileRelabels(it) },
        compileQuery = { compileQuery(it) }
    )

    fun ScopedSqlBuilder.compileSelect(select: BuiltSelectQuery) {
        selectClause(select) { compileExpr(it, false) }

        if (select.body.relation.relation != EmptyRelation) addSql("\nFROM ")

        compileQueryBody(
            select.body,
            compileExpr = { compileExpr(it, false) },
            compileRelation = { compileRelation(it) },
            compileWindows = { windows -> compileWindows(windows) }
        )
    }

    fun ScopedSqlBuilder.compileValues(query: BuiltValuesQuery): Boolean {
        return compileValues(query, compileExpr = { compileExpr(it, false) })
    }

    fun ScopedSqlBuilder.compileAssignment(assignment: Assignment<*>) {
        compileSetLhs(assignment.reference)
        addSql(" = ")
        compileExpr(assignment.expr)
    }

    fun ScopedSqlBuilder.compileInsert(insert: BuiltInsert): Boolean {
        val relvar = insert.unwrapTable()

        val insertAlias = Alias()

        compileInsertLine(insert) {
            addTableReference(relvar)
            addSql(" AS ")
            addAlias(insertAlias)
        }

        addSql("\n")

        val nonEmpty = compileQuery(insert.query)

        val builder = withColumns(relvar.columns, insertAlias)

        compileOnConflict(
            insert.onConflict,
            compileAssignment = { assignment ->
                builder.compileAssignment(assignment)
            },
            compileExpr = { expr ->
                builder.compileExpr(expr,
                    emitParens = false,
                    emitAliases = false
                )
            }
        )

        return nonEmpty
    }

    fun ScopedSqlBuilder.compileWindows(windows: List<LabeledWindow>) = compileWindowClause(windows) { window ->
        compileWindow(window)
    }

    fun ScopedSqlBuilder.compileDelete(delete: BuiltDelete) = compileDelete(delete,
        compileWiths = { compileWiths(it) },
        compileQueryBody = { query ->
            compileQueryBody(
                query,
                compileExpr = { compileExpr(it, false) },
                compileRelation = { compileRelation(it) },
                compileWindows = { compileWindows(it) }
            )
        }
    )

    private inline fun <T> ScopedSqlBuilder.scopedIn(query: PopulatesScope, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withScope(query)

        return compilation.block()
    }

    private inline fun <T> ScopedSqlBuilder.scopedCtesIn(query: BuiltQuery, block: ScopedSqlBuilder.() -> T): T {
        val compilation = withCtes(query)

        return compilation.block()
    }

    fun ScopedSqlBuilder.compileUpdate(update: BuiltUpdate) = compileUpdate(update,
        compileWiths = { compileWiths(it) },
        compileJoins = {
            check (it.isEmpty()) { "Postgres dialect does not support joins in updates" }
        },
        compileRelation = { compileRelation(it) },
        compileAssignment = { compileAssignment(it) },
        compileExpr = { compileExpr(it, false) }
    )

    override fun compile(dml: BuiltDml): CompiledSql? {
        val sql = ScopedSqlBuilder(
            CompiledSqlBuilder(PostgresDmlEscapes),
            Scope(NameRegistry { "column${it + 1}" }),
            compiler
        )

        return sql.compile(dml,
            compileQuery = { compileQuery(it) },
            compileStmt = { compileStmt(it) }
        )
    }
}