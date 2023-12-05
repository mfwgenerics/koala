plugins {
    publish
}

dependencies {
    api(projects.jdbc)
    api(libs.postgresql)
    testImplementation(projects.testing)
    testImplementation(project(":testing", "testArchive"))
}