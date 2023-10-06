package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.util.field
import org.spectralpowered.mixin.asm.util.nullField

fun MethodNode.init(cls: ClassNode) {
    this.cls = cls
    origCls = cls
    origName = name
    origDesc = desc
}

fun MethodNode.reset() {
    refsIn.clear()
    refsOut.clear()
    fieldReadRefs.clear()
    fieldWriteRefs.clear()
    classRefs.clear()
    hierarchy = null
    parents.clear()
    children.clear()
}

var MethodNode.cls: ClassNode by field()
val MethodNode.group get() = cls.group
val MethodNode.id get() = "${cls.id} $name $desc"

var MethodNode.origCls: ClassNode by field()
var MethodNode.origName: String by field()
var MethodNode.origDesc: String by field()

val MethodNode.refsIn: MutableSet<MethodNode> by field { mutableSetOf() }
val MethodNode.refsOut: MutableSet<MethodNode> by field { mutableSetOf() }
val MethodNode.fieldReadRefs: MutableSet<FieldNode> by field { mutableSetOf() }
val MethodNode.fieldWriteRefs: MutableSet<FieldNode> by field { mutableSetOf() }
val MethodNode.classRefs: MutableSet<ClassNode> by field { mutableSetOf() }

var MethodNode.hierarchy: MutableSet<MethodNode>? by nullField()
val MethodNode.parents: MutableSet<MethodNode> by field { mutableSetOf() }
val MethodNode.children: MutableSet<MethodNode> by field { mutableSetOf() }

fun MethodNode.isPrivate() = (access and ACC_PRIVATE) != 0
fun MethodNode.isAbstract() = (access and ACC_ABSTRACT) != 0
fun MethodNode.isStatic() = (access and ACC_STATIC) != 0

fun MethodNode.isConstructor() = name == "<init>"
fun MethodNode.isInitializer() = name == "<clinit>"