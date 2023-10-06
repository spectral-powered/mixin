plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
}

dependencies {
    implementation(project(":asm"))
    implementation(project(":annotations"))
    implementation(libs.bundles.kotlin)
    api("com.github.demidenko05:a-javabeans:_")
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
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "mixin-injector"
            version = project.version.toString()
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}