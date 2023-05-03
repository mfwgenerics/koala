plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://gradle.pkg.st/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    implementation("com.palantir.gradle.gitversion:gradle-git-version:0.15.0")
}
