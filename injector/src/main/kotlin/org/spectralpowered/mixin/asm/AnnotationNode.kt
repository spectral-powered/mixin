package org.spectralpowered.mixin.asm

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.Injector
import org.spectralpowered.mixin.asm.tree.ClassGroup
import org.spectralpowered.mixin.asm.tree.group
import org.spectralpowered.mixin.asm.util.field
import sun.reflect.annotation.AnnotationParser

var AnnotationNode.injector: Injector by field()

inline fun <reified T : Annotation> AnnotationNode.parse(): T? {
    if(desc != Type.getDescriptor(T::class.java)) return null
    val valueMap = mutableMapOf<String, Any>()
    if(values != null && values.isNotEmpty()) {
        var i = 0
        while(i < values.size) {
            val key = values[i] as String
            val value = values[i + 1].let { when(it) {
                is Type -> AsmClassLoader(injector.targetGroup).loadClass(it.internalName.replace("/", "."))
                else -> it
            } }
            valueMap[key] = value
            i += 2
        }
    }
    return AnnotationParser.annotationForMap(T::class.java, valueMap) as T
}

inline fun <reified T : Annotation> List<AnnotationNode>.parse(): T? {
    this.forEach { an ->
        val annotation = an.parse<T>()
        if(annotation != null) return annotation as T
    }
    return null
}

val ClassNode.annotations: List<AnnotationNode> get() {
    if(visibleAnnotations == null) visibleAnnotations = mutableListOf()
    if(invisibleAnnotations == null) invisibleAnnotations = mutableListOf()
    return visibleAnnotations.plus(invisibleAnnotations)
}

val MethodNode.annotations: List<AnnotationNode> get() {
    if(visibleAnnotations == null) visibleAnnotations = mutableListOf()
    if(invisibleAnnotations == null) invisibleAnnotations = mutableListOf()
    return visibleAnnotations.plus(invisibleAnnotations)
}

val FieldNode.annotations: List<AnnotationNode> get() {
    if(visibleAnnotations == null) visibleAnnotations = mutableListOf()
    if(invisibleAnnotations == null) invisibleAnnotations = mutableListOf()
    return visibleAnnotations.plus(invisibleAnnotations)
}