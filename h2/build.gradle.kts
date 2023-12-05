plugins {
    publish
}

dependencies {
    api(projects.jdbc)
    testImplementation(libs.h2)
    testImplementation(projects.testing)
    testImplementation(project(":testing", "testArchive"))
}