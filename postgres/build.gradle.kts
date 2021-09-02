plugins {
    id("conventions")
}

dependencies {
    implementation(project(":core"))

    implementation("org.postgresql:postgresql:42.2.23")

    testImplementation(project(":core", "testArchive"))
}
