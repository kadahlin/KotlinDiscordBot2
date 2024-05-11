rootProject.name = "KotlinDiscordBot2"
include("models", "stack", "server")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

