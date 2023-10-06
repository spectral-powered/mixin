plugins {
    id("org.spectralpowered.mixin") version "1.0.0"
    application
}

repositories {
    mavenLocal()
    maven(url = "https://maven.spectralpowered.org")
}

dependencies {
    mixin(project(":test-app:mixins"))
    mixinApi(project(":test-app:api"))
    inject(project(":test-app:target"))
    implementation("org.spectralpowered.mixin:mixin-injector:1.0.0")
}

application {
    mainClass.set("app.App")
}