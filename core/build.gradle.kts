plugins {
    id("conventions")
}

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    testImplementation("com.h2database:h2:1.4.200")
}
