package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode

fun AbstractInsnNode.isSequential() = when(this) {
    is LabelNode -> false
    is JumpInsnNode -> false
    is TableSwitchInsnNode -> false
    is LookupSwitchInsnNode -> false
    else -> opcode !in listOf(IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN, RET, ATHROW)
}
