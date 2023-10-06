package org.spectralpowered.mixin.asm.remap

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.mixin.asm.tree.ClassGroup
import org.spectralpowered.mixin.asm.tree.cls
import org.spectralpowered.mixin.asm.tree.hierarchy
import org.spectralpowered.mixin.asm.tree.id

class ClassGroupRenamer(private val group: ClassGroup) {

    private val mappings = hashMapOf<String, String>()

    fun renameClass(cls: ClassNode, newName: String) {
        mappings[cls.id] = newName
    }

    fun renameMethod(method: MethodNode, newName: String) {
        if(mappings.containsKey(method.id)) return
        method.cls.hierarchy.forEach { cls ->
            val key = "${cls.name} ${method.name} ${method.desc}"
            if(mappings.containsKey(key)) return@forEach
            mappings[key] = "${cls.name} $newName"
        }
    }

    fun moveMethod(method: MethodNode, newCls: ClassNode) {
        val newName = mappings[method.id] ?: method.name
        mappings[method.id] = "${newCls.name} $newName"
    }

    fun renameField(field: FieldNode, newName: String) {
        field.cls.hierarchy.forEach { cls ->
            val key = "${cls.name} ${field.name}"
            if(mappings.containsKey(key)) return@forEach
            mappings[key] = "${cls.name} $newName"
        }
    }

    fun moveField(field: FieldNode, newCls: ClassNode) {
        val newName = mappings[field.id] ?: field.name
        mappings[field.id] = "${newCls.name} $newName"
    }

    fun apply() {
        val remapper = ClassGroupRemapper(group, mappings)
        group.classes = remapper.remap().values.toMutableSet()
        group.init()
    }
}