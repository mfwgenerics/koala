plugins {
    id("conventions")
}

dependencies {
    api(project(":core"))

    testImplementation("com.h2database:h2:1.4.200")
}

configurations.register(
    "testArchive"
) {
    extendsFrom(configurations.testCompile.get())
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