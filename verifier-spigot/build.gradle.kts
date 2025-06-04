/*
 * ForgeLegalizerVerifier-Spigot is a SpigotMC verifier plugin for ForgeLegalizer client modification.
 * Copyright (C) 2023-2025 BromineMC
 * Copyright (C) 2023-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(17) // Needed for ViaVersion.

group = "ru.brominemc.forgelegalizerverifier"
base.archivesName = "ForgeLegalizerVerifier-Spigot"
description = "Fixes Forge player reach for 1.18.2 -> 1.19.4."

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/") // Spigot.
    maven("https://repo.viaversion.com/") // ViaVersion.
}

dependencies {
    // Spigot.
    compileOnly(libs.spigot)
    compileOnly(libs.viaversion) // Optional.
}

// Compile with UTF-8, Java 8, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 8
}

// Expand version.
tasks.withType<ProcessResources> {
    inputs.property("version", version)
    filesMatching("plugin.yml") {
        expand(inputs.properties)
    }
}

// Reproducible builds.
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Add LICENSE and manifest into the JAR file.
tasks.withType<Jar> {
    from("COPYING")
    from("NOTICE")
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizerVerifier",
            "Specification-Version" to version,
            "Specification-Vendor" to "BromineMC",
            "Implementation-Title" to "ForgeLegalizerVerifier-Spigot",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "threefusii, VidTu"
        )
    }
}
