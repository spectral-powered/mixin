plugins {
    `java-library`
    `maven-publish`
}

tasks.wrapper {
    gradleVersion = "8.2.1"
}

allprojects {
    group = "org.spectralpowered.mixin"
    version = "0.1.0"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://maven.spectralpowered.org")
    }
}

dependencies {
    api(project(":annotations"))
    api(project(":asm"))
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    from(sourceSets["main"].allJava)
    archiveClassifier.set("sources")
}

publishing {
    repositories {
        mavenLocal()
        maven(url = "https://maven.spectralpowered.org/releases") {
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "mixin"
            version = project.version.toString()
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}