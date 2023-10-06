package org.spectralpowered.mixin.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

open class MixinPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("mixin", MixinExtension::class.java, this)
        }
    }
}