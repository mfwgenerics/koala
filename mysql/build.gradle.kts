plugins {
    id("conventions")
}

dependencies {
    implementation(project(":jdbc"))

    implementation("mysql:mysql-connector-java:8.0.26")

    testImplementation(project(":jdbc", "testArchive"))
}

tasks.test {
    maxParallelForks = 16
}
