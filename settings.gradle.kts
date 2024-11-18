import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootDir.resolve("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            name = "seatgeek-sdk"
            url = uri("https://seatgeek.jfrog.io/seatgeek/seatgeek-sdk")

            credentials {
                username = localProperties.getProperty("sdkMavenUsername")
                password = localProperties.getProperty("sdkMavenPassword")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "SeatGeek SDK Sample"
include(":app")
 