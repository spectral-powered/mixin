package org.spectralpowered.mixin.asm

import org.objectweb.asm.ClassWriter
import org.spectralpowered.mixin.asm.tree.ClassGroup

class AsmClassWriter(private val group: ClassGroup, flags: Int = ClassWriter.COMPUTE_FRAMES) : ClassWriter(flags) {

    private val classLoader = AsmClassLoader(group)

    override fun getCommonSuperClass(type1: String, type2: String): String {
        var class1 = classLoader.loadClass(type1.replace("/", "."))!!
        val class2 = classLoader.loadClass(type2.replace("/", "."))!!
        return when {
            class1.isAssignableFrom(class2) -> type1
            class2.isAssignableFrom(class1) -> type2
            class1.isInterface || class2.isInterface -> "java/lang/Object"
            else -> {
                do {
                    class1 = class1.superclass!!
                } while(!class1.isAssignableFrom(class2))
                class1.name
            }
        }
    }
}