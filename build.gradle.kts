repositories {
    mavenLocal()
    maven("https://repo.erethon.de/snapshots/")
    maven("https://repo.magmaguy.com/snapshots/")
    maven("https://repo.magmaguy.com/releases/")
}

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "de.erethon"
version = "1.4-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

val papyrusVersion = "1.21.11-R0.1-SNAPSHOT"
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweightDevelopmentBundle("de.erethon.papyrus", "dev-bundle", papyrusVersion)
    compileOnly("org.geysermc.geyser:api:2.7.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.9.0")
}

tasks.register<Copy>("deployToSharedServer") {
    doNotTrackState("")
    group = "Erethon"
    description = "Used for deploying the plugin to the shared server. runServer will do this automatically." +
            "This task is only for manual deployment when running runServer from another plugin."
    dependsOn(":jar")
    from(layout.buildDirectory.file("libs/Daedalus-$version.jar"))
    into("C:\\Dev\\Erethon\\plugins")
}


tasks {
    runServer {
        if (!project.buildDir.exists()) {
            project.buildDir.mkdir()
        }
        val f = File(project.buildDir, "server.jar");
        uri("https://github.com/DRE2N/Papyrus/releases/download/latest/papyrus-paperclip-$papyrusVersion-mojmap.jar").toURL().openStream().use { it.copyTo(f.outputStream()) }
        serverJar(f)
        runDirectory.set(file("C:\\Dev\\Erethon"))
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    jar {
        manifest {
            attributes(
                "paperweight-mappings-namespace" to "mojang"
            )
        }
    }
    assemble {
        dependsOn(reobfJar)
    }
}

publishing {
    repositories {
        maven {
            name = "erethon"
            url = uri("https://repo.erethon.de/snapshots/")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = "Daedalus"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
