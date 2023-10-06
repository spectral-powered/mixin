plugins {
    alias(libs.plugins.kotlin.jvm)
    java
    `maven-publish`
}

dependencies {
    implementation(libs.bundles.kotlin)
    api(libs.bundles.asm)
    testImplementation(libs.bundles.kotlin)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        vendor.set(JvmVendorSpec.AZUL)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.AZUL)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
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
                artifactId = "mixin-asm"
                version = project.version.toString()
                from(components["java"])
                artifact(sourcesJar)
            }
        }
    }
}