package org.spectralpowered.mixin

import org.objectweb.asm.Type
import org.spectralpowered.mixin.annotations.Mixin
import org.spectralpowered.mixin.asm.annotations
import org.spectralpowered.mixin.asm.parse

class MixinApiClassesInjection(private val injector: Injector) {

    private var count = 0

    fun run() {
        println("Injecting mixin api classes.")

        injector.apiGroup.classes.forEach { apiCls ->
            injector.targetGroup.addClass(apiCls)
        }
        injector.targetGroup.init()

        injector.mixinClasses.forEach { mixinCls ->
            val mixinAnnotation = mixinCls.annotations.parse<Mixin>() ?: return@forEach
            val targetCls = injector.targetGroup.getClass(Type.getInternalName(mixinAnnotation.value.java))
                ?: error("Target class not found: ${mixinAnnotation.value.java.canonicalName}")

            mixinCls.interfaces.forEach { itf ->
                if(injector.targetGroup.getClass(itf) != null) {
                    targetCls.interfaces.add(itf)
                    count++
                }
            }
        }
        injector.targetGroup.init()

        println("Injected $count mixin api classes.")
    }
}