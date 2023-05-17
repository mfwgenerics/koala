plugins {
    id("publish")
}

dependencies {
    api(project(":jdbc"))

    testImplementation("mysql:mysql-connector-java:8.0.26")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}