rootProject.name = "DeepSeekPluginMCreator"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://mcreator.net/repo")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://mcreator.net/repo")
    }
}

// Configuración para el plugin de ShadowJar
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}