package org.spectralpowered.mixin.asm.remap

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.MemberRef
import org.spectralpowered.mixin.asm.tree.ClassGroup
import org.spectralpowered.mixin.asm.tree.init
import org.spectralpowered.mixin.asm.util.VersionUtil
import java.util.SortedMap
import java.util.TreeMap
import kotlin.math.max

class ClassGroupRemapper(private val remapper: AsmRemapper) {

    constructor(group: ClassGroup, mappings: Map<String, String>) : this(AsmRemapper(group, mappings))

    val group get() = remapper.group

    private data class Initializer(val instructions: InsnList, val maxStack: Int) {
        val dependencies = instructions.asSequence()
            .filterIsInstance<FieldInsnNode>()
            .filter { it.opcode == GETSTATIC }
            .map { MemberRef(it) }
            .toSet()
    }

    private data class Method(val owner: String, val node: MethodNode, val version: Int)
    private data class Field(val owner: String, val node: FieldNode, val version: Int, val initializer: Initializer?)

    private var classes = sortedMapOf<String, ClassNode>()

    private val methods = mutableListOf<Method>()
    private val fields = mutableMapOf<MemberRef, Field>()
    private val splicedFields = mutableSetOf<MemberRef>()

    fun remap(): SortedMap<String, ClassNode> {
        group.classes.forEach { classes[it.name] = it }

        extractFields()
        extractMethods()

        for(cls in classes.values) {
            cls.remap(remapper)
        }

        classes = classes.mapKeysTo(TreeMap()) { (_, cls) -> cls.name }

        spliceFields()
        spliceMethods()
        removeEmptyInitializers()

        return classes
    }

    private fun extractMethods() {
        for(cls in classes.values) {
            cls.methods.removeIf { method ->
                val oldOwner = remapper.mapType(cls.name)
                val newOwner = remapper.mapMethodOwner(cls.name, method.name, method.desc)

                if(oldOwner == newOwner) return@removeIf false

                method.remap(remapper, cls.name)
                methods += Method(newOwner, method, cls.version)

                return@removeIf true
            }
        }
    }

    private fun extractFields() {
        for(cls in classes.values) {
            cls.fields.removeIf { field ->
                val oldOwner = remapper.mapType(cls.name)
                val newOwner = remapper.mapFieldOwner(cls.name, field.name, field.desc)

                if(oldOwner == newOwner) return@removeIf false

                val initializer = extractInitializer(cls, field)

                field.remap(remapper, cls.name)
                val newMember = MemberRef(newOwner, field.name, field.desc)
                fields[newMember] = Field(newOwner, field, cls.version, initializer)

                return@removeIf true

            }
        }
    }

    private fun extractInitializer(cls: ClassNode, field: FieldNode): Initializer? {
        val clinit = cls.methods.find { it.name == "<clinit>" } ?: return null
        val initializer = remapper.getFieldInitializer(cls.name, field.name, field.desc) ?: return null

        val insns = InsnList()
        for(insn in initializer) {
            clinit.instructions.remove(insn)
            insns.add(insn)
            insn.remap(remapper)
        }

        return Initializer(insns, clinit.maxStack)
    }

    private fun spliceMethods() {
        for(method in methods) {
            val cls = classes.computeIfAbsent(method.owner, ::createClass)
            cls.version = VersionUtil.max(cls.version, method.version)
            cls.methods.add(method.node)
        }
    }

    private fun spliceFields() {
        for(member in fields.keys) {
            spliceField(member)
        }
    }

    private fun spliceField(member: MemberRef) {
        if(!splicedFields.add(member)) return

        val field = fields[member] ?: return
        val cls = classes.computeIfAbsent(field.owner, ::createClass)

        if(field.initializer != null) {
            for(dep in field.initializer.dependencies) {
                spliceField(dep)
            }

            val clinit = cls.methods.find { it.name == "<clinit>" } ?: createClinit(cls)
            clinit.maxStack = max(clinit.maxStack, field.initializer.maxStack)
            clinit.instructions.insertBefore(clinit.instructions.last, field.initializer.instructions)
        }

        cls.version = VersionUtil.max(cls.version, field.version)
        cls.fields.add(field.node)
    }

    private fun removeEmptyInitializers() {
        for(cls in classes.values) {
            val clinit = cls.methods.find { it.name == "<clinit>" } ?: continue
            val first = clinit.instructions.firstOrNull { it.opcode != -1 }
            if(first != null && first.opcode == RETURN) {
                cls.methods.remove(clinit)
            }
        }
    }

    private fun createClass(name: String): ClassNode {
        val node = ClassNode()
        node.version = V1_1
        node.access = ACC_PUBLIC or ACC_SUPER or ACC_FINAL
        node.name = name
        node.superName = "java/lang/Object"
        node.interfaces = mutableListOf()
        node.innerClasses = mutableListOf()
        node.fields = mutableListOf()
        node.methods = mutableListOf()
        return node
    }

    private fun createClinit(cls: ClassNode): MethodNode {
        val clinit = MethodNode()
        clinit.access = ACC_STATIC
        clinit.name = "<clinit>"
        clinit.desc = "()V"
        clinit.exceptions = mutableListOf()
        clinit.parameters = mutableListOf()
        clinit.instructions = InsnList()
        clinit.instructions.add(InsnNode(RETURN))
        clinit.tryCatchBlocks = mutableListOf()
        cls.methods.add(clinit)
        clinit.init(cls)
        return clinit
    }
}