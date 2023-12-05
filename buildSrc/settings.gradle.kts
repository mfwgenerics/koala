/* this exists to prevent gradle from recursing deep in search of a settings file */

pluginManagement {
  repositories {
    maven("https://gradle.pkg.st")
    gradlePluginPortal()
  }
}

plugins {
  id("build.less") version("1.0.0-beta9")
}

rootProject.name = "buildSrc"
