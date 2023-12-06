plugins {
    publish
}

dependencies {
    api(projects.jdbc)
    testImplementation(libs.mysql)
    testImplementation(projects.testing)
    testImplementation(project(":testing", "testArchive"))
}