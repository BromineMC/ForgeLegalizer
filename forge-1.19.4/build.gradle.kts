plugins {
    id("dev.architectury.loom") version "1.4-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.forgelegalizer"
base.archivesName = "ForgeLegalizer-Forge-1.19.4"

repositories {
    maven("https://api.modrinth.com/maven")
    maven("https://cursemaven.com")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.19.4")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:1.19.4-45.0.38") // Fixed in 45.0.39.

    // Speedup loading and testing
    modRuntimeOnly("curse.maven:lazydfu-460819:4327266")
    modRuntimeOnly("maven.modrinth:ksyxis:1.2.2")
}

loom {
    forge {
        mixinConfigs = setOf("forgelegalizer.mixins.json");
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from("LICENSE")
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizer",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu, threefusii",
            "Implementation-Title" to "ForgeLegalizer",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
