plugins {
    java
    `maven-publish`
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    from(sourceSets["main"].allJava)
    archiveClassifier.set("sources")
}

publishing {
    publishing {
        repositories {
            mavenLocal()
            maven(url = "https://maven.spectralpowered.org") {
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = "mixin-annotations"
                version = project.version.toString()
                from(components["java"])
                artifact(sourcesJar)
            }
        }
    }
}