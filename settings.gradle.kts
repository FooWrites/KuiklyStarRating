pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://mirrors.tencent.com/repository/maven-tencent/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://mirrors.tencent.com/repository/maven-tencent/")
    }
}

rootProject.name = "KuiklyStarRating"
include(":starrating")
include(":starrating-compose")
include(":demo")
include(":androidApp")
