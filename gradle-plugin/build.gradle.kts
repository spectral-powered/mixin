plugins {
    kotlin("jvm") version "1.9.10"
    `kotlin-dsl`
    `java-library`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.1.0"
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(project(":injector"))
}

gradlePlugin {
    website.set("https://spectralpowered.org/")
    vcsUrl.set("https://github.com/spectral-powered/mixin/")
    plugins {
        create("mixin") {
            id = "org.spectralpowered.mixin.plugin"
            version = project.version.toString()
            implementationClass = "org.spectralpowered.mixin.plugin.MixinPlugin"
            displayName = "Spectral Powered Mixin Plugin"
            description = "Gradle plugin for the Spectral Powered mixin framework."
            tags = listOf("spectralpowered", "mixin", "injector")
        }
    }
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    from(sourceSets["main"].allJava)
    archiveClassifier.set("sources")
}

publishing {
    repositories {
        mavenLocal()
        maven(url = "https://maven.spectralpowered.org/mixin") {
            credentials {
                username = System.getenv("MAVEN_USERNAME") ?: ""
                password = System.getenv("MAVEN_PASSWORD") ?: ""
            }
        }
    }

    publications {
        create<MavenPublication>("mixin") {
            groupId = project.group.toString()
            artifactId = "mixin-plugin"
            version = project.version.toString()
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}