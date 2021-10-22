plugins {
    id("publish")
}

dependencies {
    implementation(project(":jdbc"))

    testImplementation("org.postgresql:postgresql:42.2.23")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}
