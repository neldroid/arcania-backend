plugins {
    kotlin("jvm") version "2.3.10"
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ksp)
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

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}