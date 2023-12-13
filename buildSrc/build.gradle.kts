plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://gradle.pkg.st")
    maven("https://maven.pkg.st")
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
    implementation("com.palantir.gradle.gitversion:gradle-git-version:3.0.0")
}
