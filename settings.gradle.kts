pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TripTracker"

include(":app")

// Core modules
include(":core:common")
include(":core:domain")
include(":core:ui")

// Service modules
include(":service:location")
include(":service:activity")
include(":service:database")

// Feature modules
include(":feature:trips")
include(":feature:trip-detail")
include(":feature:active-trip")
