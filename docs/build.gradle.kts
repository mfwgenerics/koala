plugins {
    id("conventions")

    application

    id("io.koalaql.kapshot-plugin") version "0.1.1"
}

application {
    mainClass.set("MainKt")
}

dependencies {
    api(project(":h2"))

    implementation("com.h2database:h2:2.1.210")
    implementation("io.koalaql:markout:0.0.5")
    implementation("io.koalaql:markout-docusaurus:0.0.5")

    testImplementation(project(":testing"))
    testImplementation(project(":testing", "testArchive"))
}

tasks.register<DocGenTask>("generate") {
    dependsOn("test")

    finalizedBy("run")
}