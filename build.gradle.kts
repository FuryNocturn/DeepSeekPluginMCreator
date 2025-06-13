plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.deepseek.mcreator"
version = "2.0.0"

repositories {
    maven("https://mcreator.net/repo")
    mavenCentral()
}

dependencies {
    // Dependencias de MCreator
    compileOnly("net.mcreator:mcreator:2025.1")
    compileOnly("net.mcreator:blockly:2025.1")

    // DeepSeek y utilities
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.diogonunes:JColor:5.5.1")

    // Encriptación para API Key
    implementation("org.jasypt:jasypt:1.9.3")

    // UI
    implementation("com.formdev:flatlaf:3.2.1") // Para estilos modernos

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    shadowJar {
        archiveFileName.set("DeepSeekPluginMCreator.jar")
        manifest {
            attributes(
                "Plugin-Class" to "com.deepseek.mcreator.DeepSeekPlugin",
                "Plugin-Id" to "deepseek_assistant",
                "Plugin-Version" to version,
                "MCreator-Version" to "2025.1"
            )
        }
    }

    test {
        useJUnitPlatform()
    }
}