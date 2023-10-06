package org.spectralpowered.mixin.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Nested
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

open class MixinExtension (private val project: Project){

    private val mixinProjects = mutableSetOf<Project>()
    private val mixinApiProjects = mutableSetOf<Project>()
    private val injectProjects = mutableSetOf<Project>()

    lateinit var mixinsJarFile: File private set
    lateinit var mixinApiJarFile: File private set
    lateinit var injectJarFile: File private set

    var outputJarFile: File = project.buildDir.resolve("lib/target.injected.jar")

    init {
        with(project) {
            val mixinConfiguration = configurations.create("mixin")
            val mixinApiConfiguration = configurations.create("mixinApi")
            val injectConfiguration = configurations.create("inject")

            mixinConfiguration.allDependencies.withType(ProjectDependency::class.java) {
                mixinProjects.add(dependencyProject)
            }

            mixinApiConfiguration.allDependencies.withType(ProjectDependency::class.java) {
                mixinApiProjects.add(dependencyProject)
            }

            injectConfiguration.allDependencies.withType(ProjectDependency::class.java) {
                injectProjects.add(dependencyProject)
            }

            val mixinJarTask = tasks.register("mixinJar", Jar::class.java) {
                mixinProjects.forEach {
                    val jarTask = it.tasks.named("jar", Jar::class.java).get()
                    val jarFile = jarTask.archiveFile.get().asFile
                    dependsOn(jarTask)
                    from(zipTree(jarFile))
                }
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                archiveBaseName.set("mixins")
                archiveVersion.set("")
                archiveClassifier.set("")
            }

            val mixinApiJarTask = tasks.register("mixinApiJar", Jar::class.java) {
                mixinApiProjects.forEach {
                    val jarTask = it.tasks.named("jar", Jar::class.java).get()
                    val jarFile = jarTask.archiveFile.get().asFile
                    dependsOn(jarTask)
                    from(zipTree(jarFile))
                }
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                archiveBaseName.set("api")
                archiveVersion.set("")
                archiveClassifier.set("")
            }

            val targetJarTask = tasks.register("jarTarget", Jar::class.java) {
                injectProjects.forEach {
                    val jarTask = it.tasks.named("jar", Jar::class.java).get()
                    val jarFile = jarTask.archiveFile.get().asFile
                    dependsOn(jarTask)
                    from(zipTree(jarFile))
                }
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                archiveBaseName.set("target")
                archiveVersion.set("")
                archiveClassifier.set("")
            }

            val injectTask = tasks.register("inject", InjectTask::class.java) {
                group = "build"
                dependsOn(mixinJarTask)
                dependsOn(mixinApiJarTask)
                dependsOn(targetJarTask)
            }

            tasks.named("jar", Jar::class.java) {
                dependsOn(injectTask)
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from(buildDir.resolve("libs/target.injected.jar"))
            }

            tasks.named("processResources", ProcessResources::class.java) {
                dependsOn(injectTask)
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from(buildDir.resolve("libs/target.injected.jar"))
            }
        }
    }

}