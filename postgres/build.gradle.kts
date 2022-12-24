plugins {
    id("publish")
}

dependencies {
    implementation(project(":jdbc"))
    api("org.postgresql:postgresql:42.5.1")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}
