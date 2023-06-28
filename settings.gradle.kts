pluginManagement {
  repositories {
    maven("https://gradle.pkg.st/")
  }
}

plugins {
  id("build.less") version("1.0.0-beta1")
  id("com.gradle.enterprise") version("3.13")
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

include("core")
include("jdbc")
include("testing")
include("h2")
include("mysql")
include("postgres")
include("docs")

buildless {
  // the sweet sound of silence
}
