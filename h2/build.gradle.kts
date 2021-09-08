plugins {
    id("conventions")
}

dependencies {
    api(project(":jdbc"))

    implementation("com.h2database:h2:1.4.199")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}