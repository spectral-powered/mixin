package org.spectralpowered.mixin

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

    private fun init() {
        mixinGroup.readJar(mixinJar)
        apiGroup.readJar(apiJar)
        targetGroup.readJar(targetJar)
    }

    fun inject() {
        init()
        println("Running injector...")
        targetGroup.writeJar(outputJar)
    }
}