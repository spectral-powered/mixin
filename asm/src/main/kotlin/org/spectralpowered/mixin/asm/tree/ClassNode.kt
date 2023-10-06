package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.AsmClassWriter
import org.spectralpowered.mixin.asm.util.field
import org.spectralpowered.mixin.asm.util.nullField
import java.util.ArrayDeque

fun ClassNode.init(group: ClassGroup) {
    this.group = group
    origName = name
    methods.forEach { it.init(this) }
    fields.forEach { it.init(this) }
}

fun ClassNode.reset() {
    superClass = null
    interfaceClasses.clear()
    subClasses.clear()
    implementerClasses.clear()
    methodTypeRefs.clear()
    fieldTypeRefs.clear()
    methods.forEach { it.reset() }
    fields.forEach { it.reset() }
}

var ClassNode.group: ClassGroup by field()
val ClassNode.id get() = name

var ClassNode.origName: String by field()

var ClassNode.superClass: ClassNode? by nullField()
val ClassNode.interfaceClasses: MutableSet<ClassNode> by field { mutableSetOf() }
val ClassNode.subClasses: MutableSet<ClassNode> by field { mutableSetOf() }
val ClassNode.implementerClasses: MutableSet<ClassNode> by field { mutableSetOf() }

val ClassNode.methodTypeRefs: MutableSet<MethodNode> by field { mutableSetOf() }
val ClassNode.fieldTypeRefs: MutableSet<FieldNode> by field { mutableSetOf() }

val ClassNode.superHierarchy: List<ClassNode> get() = listOfNotNull(superClass).plus(interfaceClasses).flatMap { it.superHierarchy.plus(it) }
val ClassNode.subHierarchy: List<ClassNode> get() = mutableListOf(*subClasses.toTypedArray()).plus(implementerClasses).flatMap { it.subHierarchy.plus(it) }

val ClassNode.hierarchy: List<ClassNode> get() = superHierarchy.plus(subHierarchy).plus(this)

fun ClassNode.isIgnored() = group.ignoredClasses.contains(this)

fun ClassNode.getMethod(name: String, desc: String) = methods.firstOrNull { it.name == name && it.desc == desc }
fun ClassNode.getField(name: String, desc: String) = fields.firstOrNull { it.name == name && it.desc == desc }

fun ClassNode.resolveMethod(name: String, desc: String): MethodNode? {
    var ret = getMethod(name, desc)
    if(ret != null) return ret

    val queue = ArrayDeque<ClassNode>()
    superClass?.also { queue.add(it) }
    queue.addAll(interfaceClasses)

    var cur = this
    while(queue.poll()?.also { cur = it } != null) {
        ret = cur.getMethod(name, desc)
        if(ret != null) return ret

        cur.superClass?.also { queue.add(it) }
        queue.addAll(cur.interfaceClasses)
    }

    return null
}

fun ClassNode.resolveField(name: String, desc: String): FieldNode? {
    var ret = getField(name, desc)
    if(ret != null) return ret

    val queue = ArrayDeque<ClassNode>()
    superClass?.also { queue.add(it) }
    queue.addAll(interfaceClasses)

    var cur = this
    while(queue.poll()?.also { cur = it } != null) {
        ret = cur.getField(name, desc)
        if(ret != null) return ret

        cur.superClass?.also { queue.add(it) }
        queue.addAll(cur.interfaceClasses)
    }

    return null
}

fun ClassNode.fromBytes(bytes: ByteArray, flags: Int = ClassReader.EXPAND_FRAMES): ClassNode {
    val reader = ClassReader(bytes)
    reader.accept(this, flags)
    return this
}

fun ClassNode.toBytes(flags: Int = ClassWriter.COMPUTE_MAXS): ByteArray {
    val writer = AsmClassWriter(group, flags)
    this.accept(writer)
    return writer.toByteArray()
}