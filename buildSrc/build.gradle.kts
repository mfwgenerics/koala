plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://gradle.pkg.st")
    maven("https://maven.pkg.st")
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("com.palantir.gradle.gitversion:gradle-git-version:0.15.0")
}
