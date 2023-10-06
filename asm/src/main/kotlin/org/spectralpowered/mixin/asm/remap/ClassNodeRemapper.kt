package org.spectralpowered.mixin.asm.remap

import org.objectweb.asm.Opcodes.ACC_ABSTRACT
import org.objectweb.asm.Opcodes.ACC_NATIVE
import org.objectweb.asm.tree.*
import org.spectralpowered.mixin.asm.tree.getMethod
import org.spectralpowered.mixin.asm.tree.resolveMethod

fun ClassNode.remap(remapper: AsmRemapper) {
    val origName = name
    name = remapper.mapType(origName)
    signature = remapper.mapSignature(signature, false)
    superName = remapper.mapType(superName)
    interfaces = interfaces?.map(remapper::mapType)

    val origOuterCls = outerClass
    outerClass = remapper.mapType(origOuterCls)

    if(outerMethod != null) {
        outerMethod = remapper.mapMethodName(origOuterCls, outerMethod, outerMethodDesc)
        outerMethodDesc = remapper.mapMethodDesc(outerMethodDesc)
    }

    for(innerCls in innerClasses) {
        innerCls.remap(remapper)
    }

    for(field in fields) {
        field.remap(remapper, origName)
    }

    for(method in methods) {
        method.remap(remapper, origName)
    }
}

fun InnerClassNode.remap(remapper: AsmRemapper) {
    name = remapper.mapType(name)
    outerName = remapper.mapType(outerName)
    innerName = remapper.mapType(innerName)
}

fun FieldNode.remap(remapper: AsmRemapper, owner: String) {
    name = remapper.mapFieldName(owner, name, desc)
    desc = remapper.mapDesc(desc)
    signature = remapper.mapSignature(signature, true)
    value = remapper.mapValue(value)
}

fun MethodNode.remap(remapper: AsmRemapper, owner: String) {
    name = remapper.mapMethodName(owner, name, desc)
    desc = remapper.mapMethodDesc(desc)
    signature = remapper.mapSignature(signature, false)
    exceptions = exceptions.map(remapper::mapType)

    if(access and (ACC_NATIVE or ACC_ABSTRACT) == 0) {
        for(insn in instructions) {
            insn.remap(remapper)
        }

        for(tcb in tryCatchBlocks) {
            tcb.remap(remapper)
        }
    }
}

fun TryCatchBlockNode.remap(remapper: AsmRemapper) {
    type = remapper.mapType(type)
}

fun AbstractInsnNode.remap(remapper: AsmRemapper) {
    when(this) {
        is MethodInsnNode -> {
            val origOwner = owner
            owner = remapper.mapMethodOwner(origOwner, name, desc)
            name = remapper.mapMethodName(origOwner, name, desc)
            desc = remapper.mapMethodDesc(desc)
        }

        is FieldInsnNode -> {
            val origOwner = owner
            owner = remapper.mapFieldOwner(origOwner, name, desc)
            name = remapper.mapFieldName(origOwner, name, desc)
            desc = remapper.mapDesc(desc)
        }

        is TypeInsnNode -> desc = remapper.mapType(desc)
        is LdcInsnNode -> cst = remapper.mapValue(cst)
        is MultiANewArrayInsnNode -> desc = remapper.mapType(desc)
    }
}