pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven(url = "https://maven.spectralpowered.org")
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.3"
}

rootProject.name = "mixin"

include(":gradle-plugin")
include(":asm")
include(":injector")
include(":annotations")

/*include(":test-app")
include(":test-app:app")
include(":test-app:api")
include(":test-app:target")
include(":test-app:mixins")*/
