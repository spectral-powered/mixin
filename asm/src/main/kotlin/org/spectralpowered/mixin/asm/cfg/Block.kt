package org.spectralpowered.mixin.asm.cfg

import org.objectweb.asm.tree.AbstractInsnNode

class Block {

    var id = 0

    var start = 0
    var end = 0

    var next: Block? = null
    var prev: Block? = null

    val root: Block get() {
        var b = this
        var last = prev
        while(last != null) {
            b = last
            last = b.prev
        }
        return b
    }

    val branches = mutableListOf<Block>()
    val instructions = mutableListOf<AbstractInsnNode>()
}