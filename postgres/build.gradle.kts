plugins {
    id("publish")
}

dependencies {
    api(project(":jdbc"))
    api("org.postgresql:postgresql:42.5.4")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}
