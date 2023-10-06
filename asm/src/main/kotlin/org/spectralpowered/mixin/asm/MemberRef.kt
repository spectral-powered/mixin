package org.spectralpowered.mixin.asm

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.tree.cls

data class MemberRef(val owner: String, val name: String, val desc: String) : Comparable<MemberRef> {

    constructor(method: MethodNode) : this(method.cls.name, method.name, method.desc)
    constructor(field: FieldNode) : this(field.cls.name, field.name, field.desc)
    constructor(insn: MethodInsnNode) : this(insn.owner, insn.name, insn.desc)
    constructor(insn: FieldInsnNode) : this(insn.owner, insn.name, insn.desc)

    override fun compareTo(other: MemberRef): Int {
        var ret = owner.compareTo(other.owner)
        if(ret != 0) return ret

        ret = name.compareTo(other.name)
        if(ret != 0) return ret

        return desc.compareTo(other.desc)
    }

    override fun toString(): String {
        return "$owner.$name$desc"
    }
}