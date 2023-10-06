package org.spectralpowered.mixin.asm.remap

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.SourceInterpreter
import org.spectralpowered.mixin.asm.cfg.ControlFlowGraph
import org.spectralpowered.mixin.asm.stackMetadata
import org.spectralpowered.mixin.asm.tree.*
import java.util.ArrayDeque
import java.util.BitSet

class AsmRemapper(val group: ClassGroup, private val mappings: Map<String, String>) : Remapper() {

    override fun map(key: String): String? {
        return mappings[key]
    }

    fun mapMethodOwner(owner: String, name: String, desc: String): String {
        val ret = map("$owner $name $desc") ?: return mapType(owner)
        return mapType(ret.split(" ")[0])
    }

    fun mapFieldOwner(owner: String, name: String, desc: String): String {
        val ret = map("$owner $name") ?: return mapType(owner)
        return mapType(ret.split(" ")[0])
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        val ret = map("$owner $name $descriptor") ?: return name
        return ret.split(" ")[1]
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val ret = map("$owner $name") ?: return name
        return ret.split(" ")[1]
    }

    fun getFieldInitializer(owner: String, name: String, desc: String): List<AbstractInsnNode>? {
        val clinit = group.getClass(owner)?.getMethod("<clinit>", "()V") ?: return null
        val field = group.getClass(owner)?.resolveField(name, desc) ?: return null

        val insns = clinit.instructions
        var writeInsn: AbstractInsnNode? = null

        for(insn in insns) {
            if(insn.opcode == PUTSTATIC) {
                insn as FieldInsnNode
                if(insn.owner == owner && insn.name == name && insn.desc == desc) {
                    writeInsn = insn
                    break
                }
            }
        }

        if(writeInsn == null) return emptyList()

        val interp = SourceInterpreter()
        val analyzer = Analyzer(interp)
        val frames = analyzer.analyze(clinit.cls.name, clinit)

        val traced = BitSet(insns.size())
        val queue = ArrayDeque<AbstractInsnNode>()

        traced.set(insns.indexOf(writeInsn))
        queue.add(writeInsn)

        var insn: AbstractInsnNode = InsnNode(0)
        while(queue.poll()?.also { insn = it } != null) {
            val pos = insns.indexOf(insn)
            val frame = frames[pos]
            val pops = insn.stackMetadata.pops

            for(i in 0..<pops) {
                val value = frame.getStack(frame.stackSize - i - 1)
                for(insn2 in value.insns) {
                    val pos2 = insns.indexOf(insn2)
                    if(traced.get(pos2)) continue
                    traced.set(pos2)
                    queue.add(insn2)
                }
            }

            if(insn is VarInsnNode && insn.opcode in ILOAD..ALOAD) {
                val value = frame.getLocal((insn as VarInsnNode).`var`)
                for(insn2 in value.insns) {
                    val pos2 = insns.indexOf(insn2)
                    if(traced.get(pos2)) continue
                    traced.set(pos2)
                    queue.add(insn2)
                }
            }
        }



        return emptyList()
    }
}