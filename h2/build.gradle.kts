plugins {
    id("publish")
}

dependencies {
    api(project(":jdbc"))

    testImplementation("com.h2database:h2:2.1.210")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}