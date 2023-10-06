package org.spectralpowered.mixin.asm

import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.mixin.annotations.Mixin
import org.spectralpowered.mixin.asm.tree.ClassGroup

val ClassGroup.mixinClasses: List<ClassNode> get() {
    val results = mutableListOf<ClassNode>()
    classes.forEach { cls ->
        cls.annotations.forEach { an ->
            if(an.parse<Mixin>() != null) {
                results.add(cls)
            }
        }
    }
    return results
}