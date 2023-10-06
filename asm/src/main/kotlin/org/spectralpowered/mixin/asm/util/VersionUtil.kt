package org.spectralpowered.mixin.asm.util

object VersionUtil {

    fun max(v1: Int, v2: Int): Int {
        return if(swapWords(v1) >= swapWords(v2)) v1 else v2
    }

    private fun swapWords(v: Int): Int = (v shl 16) or (v ushr 16)

}