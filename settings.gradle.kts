pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "ForgeLegalizer"
include("forge-1.18.2")
include("forge-1.19.4")
include("verifier-spigot")
include("verifier-velocity")
include("verifier-bungee")
