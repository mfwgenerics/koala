pluginManagement {
  repositories {
    maven("https://gradle.pkg.st")
    gradlePluginPortal()
  }
}

plugins {
  id("build.less") version("1.0.0-beta9")
  id("com.gradle.enterprise") version("3.16")
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
  id("com.gradle.common-custom-user-data-gradle-plugin") version("1.12.1")
}

dependencyResolutionManagement {
  repositories {
    maven("https://maven.pkg.st")
    mavenCentral()
  }
}

rootProject.name = "koala"

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

buildless {
  local {
    enabled = true
  }
}

listOf(
  "core",
  "jdbc",
  "testing",
  "h2",
  "mysql",
  "postgres",
  "docs",
).forEach {
  include(it)
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")
