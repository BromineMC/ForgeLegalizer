plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)
group = "ru.vidtu.forgelegalizerverifier"
base.archivesName = "ForgeLegalizerVerifier-Spigot"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.viaversion.com/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.viaversion:viaversion-api:4.9.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from("COPYING")
    from("NOTICE")
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizerVerifier-Spigot",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu, threefusii",
            "Implementation-Title" to "ForgeLegalizerVerifier-Spigot",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
