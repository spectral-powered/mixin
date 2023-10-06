plugins {
    `java-library`
}

tasks.wrapper {
    gradleVersion = "8.2.1"
}

allprojects {
    group = "org.spectralpowered.mixin"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://maven.spectralpowered.org/")
    }
}