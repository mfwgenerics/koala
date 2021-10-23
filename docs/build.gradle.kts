plugins {
    id("conventions")
}

dependencies {
    api(project(":h2"))

    testImplementation("com.h2database:h2:1.4.200")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}

tasks.register<DocGenTask>("generate")