package mfwgenerics.kotq

import mfwgenerics.kotq.query.Performable

interface Transaction {
    /* can't be called after done */
    fun <T> perform(performable: Performable<T>): T

    /* can be called additional times with no effect */
    fun done(shouldCommit: CommitMode = CommitMode.COMMIT)
}