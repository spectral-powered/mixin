package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.util.field
import org.spectralpowered.mixin.asm.util.nullField

fun FieldNode.init(cls: ClassNode) {
    this.cls = cls
    origCls = cls
    origName = name
    origDesc = desc
}

fun FieldNode.reset() {
    readRefs.clear()
    writeRefs.clear()
    hierarchy = null
    parents.clear()
    children.clear()
}

var FieldNode.cls: ClassNode by field()
val FieldNode.group get() = cls.group
val FieldNode.id get() = "${cls.id} $name"

var FieldNode.origCls: ClassNode by field()
var FieldNode.origName: String by field()
var FieldNode.origDesc: String by field()

val FieldNode.readRefs: MutableSet<MethodNode> by field { mutableSetOf() }
val FieldNode.writeRefs: MutableSet<MethodNode> by field { mutableSetOf() }

var FieldNode.hierarchy: MutableSet<FieldNode>? by nullField()
val FieldNode.parents: MutableSet<FieldNode> by field { mutableSetOf() }
val FieldNode.children: MutableSet<FieldNode> by field { mutableSetOf() }

fun FieldNode.isPrivate() = (access and ACC_PRIVATE) != 0
fun FieldNode.isStatic() = (access and ACC_STATIC) != 0