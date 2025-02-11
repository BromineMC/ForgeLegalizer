plugins {
    id("dev.architectury.loom") version "1.9.426"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)

group = "ru.brominemc.forgelegalizer"
base.archivesName = "ForgeLegalizer-Forge-1.18.2to1.19.3"
description = "Fixes Forge player reach for 1.18.2 -> 1.19.4."

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())

    // Forge
    forge("net.minecraftforge:forge:1.18.2-40.3.3")
}

loom {
    forge {
        mixinConfigs = setOf("forgelegalizer.mixins.json");
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    filesMatching("META-INF/mods.toml") {
        expand("version" to version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizer",
            "Specification-Version" to version,
            "Specification-Vendor" to "BromineMC",
            "Implementation-Title" to "ForgeLegalizer",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
