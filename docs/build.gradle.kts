import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("conventions")

    id("io.koalaql.markout") version "0.0.6"
    id("io.koalaql.kapshot-plugin") version "0.1.1"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"

markout {
    mainClass = "MainKt"
}

dependencies {
    api(project(":h2"))

    implementation("com.h2database:h2:2.1.210")
    implementation("io.koalaql:markout:0.0.6")
    implementation("io.koalaql:markout-docusaurus:0.0.6")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}
