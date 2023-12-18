plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
java.toolchain.languageVersion = JavaLanguageVersion.of(11)
group = "ru.vidtu.forgelegalizerverifier"
base.archivesName = "ForgeLegalizerVerifier-Velocity"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.0.0-SNAPSHOT")
    compileOnly("com.viaversion:viaversion-api:4.9.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("velocity-plugin.json") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from("COPYING")
    from("NOTICE")
    manifest {
        attributes(
            "Specification-Title" to "ForgeLegalizerVerifier-Velocity",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu, threefusii",
            "Implementation-Title" to "ForgeLegalizerVerifier-Velocity",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, threefusii"
        )
    }
}
