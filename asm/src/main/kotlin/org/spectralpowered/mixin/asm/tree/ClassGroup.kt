package org.spectralpowered.mixin.asm.tree

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import java.io.File
import java.util.ArrayDeque
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class ClassGroup {

    var classes = mutableSetOf<ClassNode>()
    val ignoredClasses = mutableSetOf<ClassNode>()

    fun addClass(cls: ClassNode, ignored: Boolean = false): Boolean {
        if(hasClass(cls)) return false
        if(ignored) ignoredClasses.add(cls) else classes.add(cls)
        return true
    }

    fun removeClass(cls: ClassNode): Boolean {
        if(!hasClass(cls)) return false
        return (if(cls.isIgnored()) ignoredClasses.remove(cls) else classes.remove(cls))
    }

    fun hasClass(cls: ClassNode): Boolean {
        return classes.any { it.name == cls.name } || ignoredClasses.any { it.name == cls.name }
    }

    fun ignoreClass(cls: ClassNode) {
        if(cls.isIgnored()) return
        classes.remove(cls)
        ignoredClasses.add(cls)
    }

    fun replaceClass(old: ClassNode, new: ClassNode) {
        removeClass(old)
        addClass(new)
    }

    fun getClass(name: String) = classes.firstOrNull { it.name == name }

    fun readJar(file: File, ignoreFilter: (ClassNode) -> Boolean = { false }) {
        JarFile(file).use { jar ->
            jar.entries().asSequence().forEach { entry ->
                if(entry.name.endsWith(".class")) {
                    val cls = ClassNode().fromBytes(jar.getInputStream(entry).readBytes(), ClassReader.EXPAND_FRAMES)
                    addClass(cls, ignored = ignoreFilter(cls))
                }
            }
        }
        init()
    }

    fun writeJar(file: File, writeIgnoredClasses: Boolean = true) {
        if(file.exists()) file.deleteRecursively()
        if(file.parentFile != null && file.parentFile.isDirectory && !file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()

        JarOutputStream(file.outputStream()).use { jos ->
            val clsList = mutableListOf<ClassNode>().also { it.addAll(classes) }
            if(writeIgnoredClasses) clsList.addAll(ignoredClasses)
            clsList.forEach { cls ->
                jos.putNextEntry(JarEntry("${cls.name}.class"))
                jos.write(cls.toBytes(ClassWriter.COMPUTE_FRAMES))
                jos.closeEntry()
            }
        }
    }

    fun clear() {
        classes.clear()
        ignoredClasses.clear()
    }

    fun init() {
        classes.forEach { it.reset() }
        classes.forEach { it.init(this) }
        repeat(4) { step ->
            classes.forEach { cls ->
                when(step) {
                    0 -> processA(cls)
                    1 -> processB(cls)
                    2 -> processC(cls)
                    3 -> processD(cls)
                }
            }
        }
    }

    private fun processA(cls: ClassNode) {
        if(cls.superClass == null && cls.superName != null) {
            cls.superClass = getClass(cls.superName)
            cls.superClass?.subClasses?.add(cls)
        }
        cls.interfaces.mapNotNull { getClass(it) }.forEach { itf ->
            cls.interfaceClasses.add(itf)
                itf.implementerClasses.add(cls)
        }
    }

    private fun processB(cls: ClassNode) {
        cls.methods.forEach { method ->
            for(insn in method.instructions) {
                when(insn) {
                    is MethodInsnNode -> {
                        val owner = getClass(insn.owner) ?: continue
                        val dst = owner.resolveMethod(insn.name, insn.desc) ?: continue
                        dst.refsIn.add(method)
                        method.refsOut.add(dst)
                        dst.cls.methodTypeRefs.add(method)
                        method.classRefs.add(dst.cls)
                    }

                    is FieldInsnNode -> {
                        val owner = getClass(insn.owner) ?: continue
                        val dst = owner.resolveField(insn.name, insn.desc) ?: continue
                        if(insn.opcode == GETFIELD || insn.opcode == GETSTATIC) {
                            dst.readRefs.add(method)
                            method.fieldReadRefs.add(dst)
                        } else {
                            dst.writeRefs.add(method)
                            method.fieldWriteRefs.add(dst)
                        }
                        dst.cls.methodTypeRefs.add(method)
                        method.classRefs.add(dst.cls)
                    }

                    is TypeInsnNode -> {
                        val dst = getClass(insn.desc) ?: continue
                        dst.methodTypeRefs.add(method)
                        method.classRefs.add(dst)
                    }

                    is MultiANewArrayInsnNode -> {
                        val dst = getClass(insn.desc) ?: continue
                        dst.methodTypeRefs.add(method)
                        method.classRefs.add(dst)
                    }
                }
            }
        }
    }

    private fun processC(cls: ClassNode) {
        if(cls.subClasses.isNotEmpty() || cls.implementerClasses.isNotEmpty()) return

        val methods = hashMapOf<String, MethodNode>()
        val fields = hashMapOf<String, FieldNode>()
        val queue = ArrayDeque<ClassNode>()

        var cur = cls
        queue.add(cur)
        while(queue.poll()?.also { cur = it } != null) {
            for(method in cur.methods) {
                var prev = method
                if(method.isHierarchyBarrier()) {
                    if(method.hierarchy == null) {
                        method.hierarchy = mutableSetOf(method)
                    }
                } else if(methods["${method.name}${method.desc}"]?.also { prev = it } != null) {
                    if(method.hierarchy == null) {
                        method.hierarchy = prev.hierarchy
                        method.hierarchy!!.add(method)
                    } else if(method.hierarchy != prev.hierarchy) {
                        for(m in prev.hierarchy!!) {
                            method.hierarchy!!.add(m)
                            m.hierarchy = method.hierarchy
                        }
                    }
                } else {
                    methods["${method.name}${method.desc}"] = method
                    if(method.hierarchy == null) {
                        method.hierarchy = mutableSetOf(method)
                    }
                }
            }

            for(field in cur.fields) {
                var prev = field
                if(field.isHierarchyBarrier()) {
                    if(field.hierarchy == null) {
                        field.hierarchy = mutableSetOf(field)
                    }
                } else if(fields["${field.name}:${field.desc}"]?.also { prev = it } != null) {
                    if(field.hierarchy == null) {
                        field.hierarchy = prev.hierarchy
                        field.hierarchy!!.add(field)
                    } else if(field.hierarchy != prev.hierarchy) {
                        for(f in prev.hierarchy!!) {
                            field.hierarchy!!.add(f)
                            f.hierarchy = field.hierarchy
                        }
                    }
                } else {
                    fields["${field.name}:${field.desc}"] = field
                    if(field.hierarchy == null) {
                        field.hierarchy = mutableSetOf(field)
                    }
                }
            }

            cur.superClass?.also { queue.add(it) }
            queue.addAll(cur.interfaceClasses)
        }
    }

    private fun processD(cls: ClassNode) {
        val queue = ArrayDeque<ClassNode>()
        val visited = mutableSetOf<ClassNode>()

        for(method in cls.methods) {
            if(method.hierarchy!!.size > 1) {
                if(method.isConstructor() || method.isInitializer()) continue
                if(method.isHierarchyBarrier()) continue

                cls.superClass?.also { queue.add(it) }
                queue.addAll(cls.interfaceClasses)
                var cur = cls
                while(queue.poll()?.also { cur = it } != null) {
                    if(!visited.add(cur)) continue
                    val m = cur.getMethod(method.name, method.desc)
                    if(m != null && !m.isHierarchyBarrier()) {
                        method.parents.add(m)
                        m.children.add(method)
                    }
                    cur.superClass?.also { queue.add(it) }
                    queue.addAll(cur.interfaceClasses)
                }
            }
            visited.clear()
        }

        queue.clear()
        visited.clear()

        for(field in cls.fields) {
            if(field.hierarchy!!.size > 1) {
                if(field.isHierarchyBarrier()) continue

                cls.superClass?.also { queue.add(it) }
                queue.addAll(cls.interfaceClasses)
                var cur = cls
                while(queue.poll()?.also { cur = it } != null) {
                    if(!visited.add(cur)) continue
                    val f = cur.getField(field.name, field.desc)
                    if(f != null && !f.isHierarchyBarrier()) {
                        field.parents.add(f)
                        f.children.add(field)
                    }
                    cur.superClass?.also { queue.add(it) }
                    queue.addAll(cur.interfaceClasses)
                }
            }
            visited.clear()
        }
    }

    private fun MethodNode.isHierarchyBarrier(): Boolean = (access and (ACC_PRIVATE or ACC_STATIC)) != 0
    private fun FieldNode.isHierarchyBarrier(): Boolean = (access and (ACC_PRIVATE or ACC_STATIC)) != 0
}