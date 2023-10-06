plugins {
    kotlin("jvm") version "1.9.10"
    `kotlin-dsl`
    `java-library`
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.spectralpowered.mixin"
version = "1.0.0"

tasks.wrapper {
    gradleVersion = "8.2.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://maven.spectralpowered.org")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation("org.spectralpowered.mixin:mixin-injector:1.0.0")
}

gradlePlugin {
    plugins {
        create("gradle-plugin") {
            id = "org.spectralpowered.mixin"
            version = project.version.toString()
            implementationClass = "org.spectralpowered.mixin.plugin.MixinPlugin"
        }
    }
}