package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.spectralpowered.mixin.asm.stackMetadata

private val ANY_INSN = { _: AbstractInsnNode -> true }

fun getExpr(last: AbstractInsnNode, filter: (AbstractInsnNode) -> Boolean = ANY_INSN): List<AbstractInsnNode>? {
    val expr = mutableListOf<AbstractInsnNode>()
    var height = 0
    var insn: AbstractInsnNode? = last

    do {
        val (pops, pushes) = insn!!.stackMetadata
        if(insn !== last) {
            expr.add(insn)
            height -= pushes
        }
        height += pops

        if(height == 0) {
            return expr.asReversed()
        }

        insn = insn.previous
    } while(insn != null && insn.isSequential() && filter(insn))

    return null
}

fun InsnList.replaceExpr(last: AbstractInsnNode, replacement: AbstractInsnNode, filter: (AbstractInsnNode) -> Boolean = ANY_INSN): Boolean {
    val expr = getExpr(last, filter) ?: return false
    expr.forEach(this::remove)
    this[last] = replacement
    return true
}

fun InsnList.deleteExpr(last: AbstractInsnNode, filter: (AbstractInsnNode) -> Boolean = ANY_INSN): Boolean {
    val expr = getExpr(last, filter) ?: return false
    expr.forEach(this::remove)
    remove(last)
    return true
}