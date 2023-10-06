plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
}

dependencies {
    api(project(":asm"))
    api(project(":annotations"))
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
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "mixin-${project.name}"
            version = project.version.toString()
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}