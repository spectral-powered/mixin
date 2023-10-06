package org.spectralpowered.mixin

import org.spectralpowered.mixin.annotations.RawMixin
import org.spectralpowered.mixin.asm.AsmClassLoader
import org.spectralpowered.mixin.asm.annotations
import org.spectralpowered.mixin.asm.parse
import org.spectralpowered.mixin.asm.tree.ClassGroup

class RawMixinInjection(private val injector: Injector) {

    fun run() {
        val mixinClassLoader = AsmClassLoader(injector.mixinGroup)
        injector.mixinGroup.classes.forEach { mixinCls ->
            val rawMixinAnnotation = mixinCls.annotations.parse<RawMixin>()
            if(rawMixinAnnotation != null)  {
                val rawMixinKlass = mixinClassLoader.loadClass(mixinCls.name.replace("/", ".")) ?: error("Could not load raw-mixin class: ${mixinCls.name}")
                println("Running raw mixin injection: ${rawMixinKlass.simpleName}.")
                rawMixinKlass.getDeclaredConstructor(ClassGroup::class.java).newInstance(injector.targetGroup)
            }
        }
        injector.targetGroup.init()
    }
}