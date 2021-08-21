package mfwgenerics.kotq.ddl

interface TypeConverter<F, T> {
    fun convert(from: F): T
    fun unconvert(from: T): F
}