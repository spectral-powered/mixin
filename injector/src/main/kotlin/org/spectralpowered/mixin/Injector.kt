package org.spectralpowered.mixin

import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.mixin.asm.annotations
import org.spectralpowered.mixin.asm.injector
import org.spectralpowered.mixin.asm.mixinClasses
import org.spectralpowered.mixin.asm.tree.ClassGroup
import java.io.File

class Injector(
    private val mixinJar: File,
    private val apiJar: File,
    private val targetJar: File,
    private val outputJar: File
) {

    val mixinGroup = ClassGroup()
    val apiGroup = ClassGroup()
    val targetGroup = ClassGroup()

    lateinit var mixinClasses: List<ClassNode> private set

    private fun init() {
        println("Starting injector...")

        mixinGroup.readJar(mixinJar)
        apiGroup.readJar(apiJar)
        targetGroup.readJar(targetJar)

        /*
         * Preload and cache all the mixin classes
         */

        mixinGroup.classes.forEach { cls ->
            cls.annotations.forEach { an ->
                an.injector = this
            }
            cls.methods.forEach { method ->
                method.annotations.forEach { an ->
                    an.injector = this
                }
            }
            cls.fields.forEach { field ->
                field.annotations.forEach { an ->
                    an.injector = this
                }
            }
        }

        mixinClasses = mixinGroup.mixinClasses
        println("Found ${mixinClasses.size} mixins to inject.")
    }

    fun inject() {
        init()
        println("Running injector...")

        /*
         * Run all the injections needed.
         */
        RawMixinInjection(this).run()
        MixinApiClassesInjection(this).run()
        MixinInjection(this).run()

        println("Finished all injections.")

        /*
         * Write the injected / modified classes in the targetgroup to the output jar.
         */
        targetGroup.writeJar(outputJar)

        /*
         * Injector completed
         */
        println("Injector completed successfully.")
    }
}