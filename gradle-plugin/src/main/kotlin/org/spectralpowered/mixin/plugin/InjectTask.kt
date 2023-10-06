package org.spectralpowered.mixin.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.spectralpowered.mixin.Injector

open class InjectTask : DefaultTask() {

    private val extension = project.extensions.getByName("mixin") as MixinExtension

    @TaskAction
    fun run() {
        val mixinsJar = project.buildDir.resolve("libs/mixins.jar")
        val mixinApiJar = project.buildDir.resolve("libs/api.jar")
        val targetJar = project.buildDir.resolve("libs/target.jar")
        val outputJar = project.buildDir.resolve("libs/target.injected.jar")

        val injector = Injector(
            mixinsJar,
            mixinApiJar,
            targetJar,
            outputJar
        )

        injector.inject()
    }
}