plugins {
    kotlin("jvm") version "2.3.10"
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.shadow)
}

group = "com.ezepiko"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.ktorEcosystem)
    implementation(libs.bundles.koinEcosystem)
    implementation(libs.kotlinxSerialization)
    implementation(libs.bundles.koinEcosystem)
    implementation(libs.bundles.databaseEcosystem)
    implementation(libs.bundles.koogEcosystem)
    implementation(libs.bundles.firebaseEcosystem)
    implementation(libs.stripe)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()

    mergeServiceFiles("META-INF/services")
    mergeServiceFiles("META-INF/io.netty.versions.properties")
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    manifest {
        attributes["Main-Class"] = "com.ezepiko.AppKt"
    }
}

// Make the standard 'build' task also produce the shadow jar
tasks.build {
    dependsOn(tasks.shadowJar)
}
tasks.test {
    useJUnitPlatform()
}