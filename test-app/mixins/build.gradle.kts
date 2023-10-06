dependencies {
    compileOnly(project(":test-app:target"))
    compileOnly(project(":test-app:api"))
    implementation("org.spectralpowered.mixin:mixin-annotations:1.0.0")
}