package org.spectralpowered.mixin.asm

import org.spectralpowered.mixin.asm.tree.ClassGroup
import org.spectralpowered.mixin.asm.tree.toBytes
import java.net.URLClassLoader

class AsmClassLoader(private val group: ClassGroup, parent: ClassLoader = AsmClassLoader::class.java.classLoader) : URLClassLoader(arrayOf(), parent) {

    override fun findClass(name: String): Class<*> {
        val asmName = name.replace(".", "/")
        val cls = group.getClass(asmName) ?: return super.findClass(name)
        val bytes = cls.toBytes()
        return super.defineClass(name, bytes, 0, bytes.size)
    }

}