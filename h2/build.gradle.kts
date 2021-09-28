plugins {
    id("publish")
}

dependencies {
    api(project(":jdbc"))

    implementation("com.h2database:h2:1.4.200")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}