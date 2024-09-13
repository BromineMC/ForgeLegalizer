plugins {
    id("dev.architectury.loom") version "1.7.413"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.brominemc.forgelegalizer"
base.archivesName = "ForgeLegalizer-Forge-1.18.2to1.19.3"
description = "Fixes Forge player reach for 1.18.2 -> 1.19.4."

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven/")
    maven("https://cursemaven.com/")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:1.18.2-40.2.21")

    // Speedup loading and testing
    modRuntimeOnly("curse.maven:lazydfu-460819:3544496")
    modRuntimeOnly("maven.modrinth:ksyxis:1.3.2")
}

loom {
    forge {
        mixinConfigs = setOf("forgelegalizer.mixins.json");
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizer",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "BromineMC",
            "Implementation-Title" to "ForgeLegalizer",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
