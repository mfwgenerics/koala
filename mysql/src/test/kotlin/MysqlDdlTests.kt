import io.koalaql.ReconcileColumns
import io.koalaql.ReconcileIndexes
import io.koalaql.ReconcileMode
import io.koalaql.ReconcileTables
import io.koalaql.ddl.UnmappedDataType
import kotlin.test.Test

class MysqlDdlTests: DdlTests() {
    override fun connect(db: String) = MysqlTestDatabase(db,
        declareBy = ReconcileTables(
            create = ReconcileMode.APPLY,

            columns = ReconcileColumns(
                add = ReconcileMode.APPLY,
                modify = ReconcileMode.APPLY,
                drop = ReconcileMode.APPLY
            ),

            indexes = ReconcileIndexes(
                add = ReconcileMode.APPLY,
                drop = ReconcileMode.APPLY
            ),

            drop = ReconcileMode.APPLY
        )
    )

    override fun supportedColumnTypes(type: UnmappedDataType<*>): Boolean {
        return true
    }

    @Test
    fun empty() { }
}