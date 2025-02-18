plugins {
    alias(libs.plugins.architectury.loom)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)

group = "ru.brominemc.forgelegalizer"
base.archivesName = "ForgeLegalizer-Forge-1.18.2to1.19.3"
description = "Fixes Forge player reach for 1.18.2 -> 1.19.4."

loom {
    silentMojangMappingsLicense()
    forge {
        mixinConfigs = setOf("forgelegalizer.mixins.json")
    }
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Allow hot swapping on supported JVM.
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+AllowRedefinitionToAddDeleteMethods",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true"
        )
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName = "forgelegalizer.mixins.refmap.json"
    }
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft.mc1182)
    mappings(loom.officialMojangMappings())

    // Forge
    forge(libs.forge.mc1182)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    filesMatching("META-INF/mods.toml") {
        expand(inputs.properties)
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizer",
            "Specification-Version" to version,
            "Specification-Vendor" to "BromineMC",
            "Implementation-Title" to "ForgeLegalizer-Forge-1.18.2to1.19.3",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
