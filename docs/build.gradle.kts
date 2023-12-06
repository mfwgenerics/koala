import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("conventions")
    alias(libs.plugins.markout)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"

markout {
    mainClass = "MainKt"
}

dependencies {
    api(projects.h2)
    api(projects.mysql)
    api(projects.postgres)
    implementation(libs.h2)
    testImplementation(projects.testing)
    testImplementation(project(":testing", "testArchive"))
}