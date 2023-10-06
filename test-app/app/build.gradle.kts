plugins {
    //id("org.spectralpowered.mixin") version "1.0.0"
    application
}

repositories {
    maven(url = "https://maven.spectralpowered.org")
}

dependencies {
    //mixin(project(":test-app:mixins"))
    //mixinApi(project(":test-app:api"))
    //inject(project(":test-app:target"))
    implementation("org.spectralpowered:mixin-injector:0.1.0-pre1")
    implementation("org.spectralpowered:mixin-annotations:0.1.0-pre1")
    implementation(project(":test-app:api"))
}

application {
    mainClass.set("app.App")
}