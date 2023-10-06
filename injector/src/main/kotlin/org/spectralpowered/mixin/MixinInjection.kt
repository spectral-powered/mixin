package org.spectralpowered.mixin

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.spectralpowered.mixin.annotations.Inject
import org.spectralpowered.mixin.annotations.Mixin
import org.spectralpowered.mixin.annotations.Overwrite
import org.spectralpowered.mixin.annotations.Shadow
import org.spectralpowered.mixin.asm.annotations
import org.spectralpowered.mixin.asm.parse
import org.spectralpowered.mixin.asm.tree.cls
import org.spectralpowered.mixin.asm.tree.getMethod
import org.spectralpowered.mixin.asm.tree.id
import java.util.*

class MixinInjection(private val injector: Injector) {

    fun run() {
        println("Injecting mixins.")

        injector.mixinClasses.forEach { mixinCls ->
            val mixinAnnotation = mixinCls.annotations.parse<Mixin>() ?: error("Failed to find @Mixin annotation.")
            val targetCls = injector.targetGroup.getClass(mixinAnnotation.value.java.canonicalName.replace(".", "/"))
                ?: error("Could not find mixin's target class: ${mixinAnnotation.value.java.canonicalName}")

            println("Injecting mixin: ${mixinCls.name}.")
            inject(mixinCls, targetCls)
        }

        /*
         * Finally reinitialize the target classgroup to rebuild the classnode info graph.
         */
        injector.targetGroup.init()
    }

    private fun inject(mixinCls: ClassNode, targetCls: ClassNode) {
        val shadowMap = hashMapOf<String, String>()

        /*
         * Build the replaceTypes map from shadow and injected references in mixin class.
         */
        mixinCls.methods.forEach { mixinMethod ->
            val shadowAnnotation = mixinMethod.annotations.parse<Shadow>()
            if(shadowAnnotation != null) {
                shadowMap["${mixinMethod.cls.name} ${mixinMethod.name} ${mixinMethod.desc}"] = mixinMethod.name.replace("shadow$", "") + " " + mixinMethod.desc
            }
        }

        mixinCls.fields.forEach { mixinField ->
            val shadowAnnotation = mixinField.annotations.parse<Shadow>()
            if(shadowAnnotation != null) {
                shadowMap["${mixinField.cls.name} ${mixinField.name} ${mixinField.desc}"] = mixinField.name.replace("shadow$", "") + " " + mixinField.desc
            }
        }

        /*
         * Inject all the methods and fields with the @Inject annotation.
         */
        mixinCls.methods.forEach { mixinMethod ->
            val injectAnnotation = mixinMethod.annotations.parse<Inject>()
            if(injectAnnotation != null) {
                val mixinMethodCopy = MethodNode(
                    mixinMethod.access,
                    mixinMethod.name,
                    mixinMethod.desc,
                    mixinMethod.signature,
                    mixinMethod.exceptions.toTypedArray().copyOf(),
                )
                mixinMethod.accept(mixinMethodCopy)
                targetCls.methods.add(mixinMethodCopy)
            }
        }

        /*
         * Inject shadowed methods as a copy for calling in overwritten mixin method injections.
         */
        mixinCls.methods.forEach { mixinMethod ->
            val shadowAnnotation = mixinMethod.annotations.parse<Shadow>()
            if(shadowAnnotation != null) {
                val shadow = shadowMap["${mixinMethod.cls.name} ${mixinMethod.name} ${mixinMethod.desc}"]!!.split(" ")
                val unshadowName = shadow[0]
                val unshadowDesc = shadow[1]
                val targetMethod = targetCls.getMethod(unshadowName, unshadowDesc) ?: error("@Shadow target method not found. [mixin-method: ${mixinMethod.id}]")

                val mixinMethodCopy = MethodNode(
                    targetMethod.access,
                    mixinMethod.name,
                    targetMethod.desc,
                    targetMethod.signature,
                    targetMethod.exceptions.toTypedArray().copyOf()
                )
                targetMethod.accept(mixinMethodCopy)
                targetCls.methods.add(mixinMethodCopy)
            }
        }

        mixinCls.methods.forEach { mixinMethod ->
            val overwriteAnnotation = mixinMethod.annotations.parse<Overwrite>()
            if(overwriteAnnotation != null) {
                val targetMethod = targetCls.getMethod(mixinMethod.name, mixinMethod.desc) ?: error("Could not find target method for @Overwrite. [mixin-method: ${mixinMethod.id}]")
                targetMethod.instructions = mixinMethod.instructions
            }
        }

        /*
         * Replace all the type references in the target class if theyre still pointing to the mixin class.
         */
        targetCls.methods.forEach { targetMethod ->
            for(insn in targetMethod.instructions) {
                when(insn) {
                    is MethodInsnNode -> {
                        if(insn.owner == mixinCls.name) {
                            insn.owner = targetCls.name
                        }
                    }

                    is FieldInsnNode -> {
                        if(shadowMap.containsKey("${insn.owner} ${insn.name} ${insn.desc}")) {
                            val shadow = shadowMap["${insn.owner} ${insn.name} ${insn.desc}"]!!.split(" ")
                            insn.name = shadow[0]
                            insn.desc = shadow[1]
                        }
                        if(insn.owner == mixinCls.name) {
                            insn.owner = targetCls.name
                        }
                    }
                }
            }
        }
    }
}