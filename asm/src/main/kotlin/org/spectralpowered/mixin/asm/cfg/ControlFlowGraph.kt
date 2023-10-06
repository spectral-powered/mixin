package org.spectralpowered.mixin.asm.cfg

import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue
import org.spectralpowered.mixin.asm.tree.cls
import org.spectralpowered.mixin.asm.tree.isSequential

class ControlFlowGraph(private val method: MethodNode) : Analyzer<BasicValue>(BasicInterpreter()) {

    val blocks = mutableListOf<Block>()

    init {
        analyze(method.cls.name, method)
    }

    override fun init(owner: String, method: MethodNode) {
        val insns = method.instructions

        var block = Block()
        block.id = blocks.size
        block.instructions.add(insns.first)
        blocks.add(block)

        for(i in 1..<insns.size()) {
            val insn = insns[i]
            block.end++
            block.instructions.add(insn)
            if(insn.next == null) break
            if(insn.next is LabelNode || insn.isSequential()) {
                block = Block()
                block.id = blocks.size
                block.start = i + 1
                block.end = i + 1
                blocks.add(block)
            }
        }
    }

    override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
        val b1 = blocks.first { insnIndex in it.start .. it.end }
        val b2 = blocks.first { successorIndex in it.start .. it.end }
        if(b1 != b2) {
            if(insnIndex + 1 == successorIndex) {
                b1.next = b2
                b2.prev = b1
            } else {
                b1.branches.add(b2)
            }
        }
    }
}