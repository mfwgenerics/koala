package io.koalaql

sealed interface DeclareStrategy {
    /* only registers type mappings and doesn't execute any ddl */
    object RegisterOnly: DeclareStrategy
    /* execute "create table if not exists" ddl */
    object CreateIfNotExists: DeclareStrategy
    /* use JDBC metadata to compute and apply a full diff. TODO parameterize */
    object Change: DeclareStrategy
    /* use JDBC metadata to error on differences from expected schema */
    object Expect: DeclareStrategy
}
