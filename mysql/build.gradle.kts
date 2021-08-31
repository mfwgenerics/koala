plugins {
    id("conventions")
}

dependencies {
    implementation(project(":core"))

    implementation("mysql:mysql-connector-java:8.0.26")
}
