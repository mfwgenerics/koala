plugins {
    id("publish")
}

dependencies {
    implementation(project(":jdbc"))

    implementation("mysql:mysql-connector-java:8.0.26")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}