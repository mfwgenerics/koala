plugins {
    id("conventions")
}

dependencies {
    api(project(":jdbc"))
}

configurations.register(
    "testArchive"
) {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Jar>(
    name = "jarTest"
) {
    from(project.sourceSets.test.get().output)
    description = "create a jar from the test source set"
    archiveClassifier.set("test")
}

artifacts {
    add("testArchive", tasks.getByName("jarTest"))
}