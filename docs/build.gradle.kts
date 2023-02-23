import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("conventions")

    id("io.koalaql.markout-docusaurus") version "0.0.11"
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"

markout {
    mainClass = "MainKt"
}

dependencies {
    api(project(":h2"))

    implementation("com.h2database:h2:2.1.210")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}
