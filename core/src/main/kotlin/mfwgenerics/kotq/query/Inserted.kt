package mfwgenerics.kotq.query

import mfwgenerics.kotq.query.built.BuildsIntoInsert

interface Inserted: Performable<Unit>, BuildsIntoInsert