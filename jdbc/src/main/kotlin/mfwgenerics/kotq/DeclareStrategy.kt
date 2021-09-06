package mfwgenerics.kotq

sealed interface DeclareStrategy {
    /* only registers type mappings and doesn't execute any ddl */
    object RegisterOnly: DeclareStrategy
    /* execute "create table if not exists" ddl */
    object CreateIfNotExists: DeclareStrategy
    /* use JDBC metadata to compute and apply a full diff. TODO parameterize */
    object Diff: DeclareStrategy
}
