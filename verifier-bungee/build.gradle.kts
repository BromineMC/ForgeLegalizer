plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)
group = "ru.brominemc.forgelegalizerverifier"
base.archivesName = "ForgeLegalizerVerifier-Bungee"
description = "Fixes Forge player reach for 1.18.2 -> 1.19.4."

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("bungee.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizerVerifier-Bungee",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "BromineMC",
            "Implementation-Title" to "ForgeLegalizerVerifier-Bungee",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "threefusii, VidTu"
        )
    }
}
