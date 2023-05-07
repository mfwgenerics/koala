pluginManagement {
  repositories {
    maven("https://gradle.pkg.st/")
  }
}

plugins {
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

val cacheUsername: String? by settings
val cachePassword: String? by settings
val cachePush: String? by settings
val remoteCache = System.getenv("GRADLE_CACHE_REMOTE")?.toBoolean() ?: false
val localCache = System.getenv("GRADLE_CACHE_LOCAL")?.toBoolean() ?: true

buildCache {
  local {
    isEnabled = localCache
  }

  if (remoteCache) {
    remote<HttpBuildCache> {
      isEnabled = true
      isUseExpectContinue = true
      isPush = System.getenv("GRADLE_CACHE_PUSH") != "false" || System.getenv("CI") == "true"

      url = uri(System.getenv("CACHE_ENDPOINT") ?: "https://gradle.less.build/cache/generic/")
      credentials {
        username = "apikey"
        password = System.getenv("BUILDLESS_APIKEY")
      }
    }
  }
}
