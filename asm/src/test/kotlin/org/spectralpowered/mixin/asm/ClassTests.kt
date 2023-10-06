package org.spectralpowered.mixin.asm

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.mixin.asm.tree.ClassGroup
import org.spectralpowered.mixin.asm.tree.fromBytes
import org.spectralpowered.mixin.asm.tree.isIgnored
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassTests {

    private val group = ClassGroup()

    @BeforeEach
    fun setup() {
        group.clear()

        TEST_CLASSES.forEach { className ->
            val bytes = ClassTests::class.java.getResourceAsStream("/$className.class")!!.readBytes()
            val cls = ClassNode().fromBytes(bytes)
            group.addClass(cls)
        }

        group.init()
    }

    @Test
    fun `adding classes`() {
        assertTrue { group.classes.size == 3 }
    }

    @Test
    fun `removing classes`() {
        val cls = group.getClass("TestClass")!!
        assertTrue { group.classes.size == 3 }

        group.removeClass(cls)
        assertTrue { group.classes.size == 2 }
    }

    @Test
    fun `ignoring class`() {
        val cls = group.getClass("TestClass")!!
        assertFalse { cls.isIgnored() }

        group.ignoreClass(cls)
        assertTrue { cls.isIgnored() }
    }

    @Test
    fun `writing classgroup`() {
        val tmpFile = File.createTempFile("tmp", ".jar")
        tmpFile.deleteOnExit()

        group.writeJar(tmpFile)
        val bytes = tmpFile.readBytes()
        assertTrue { bytes.isNotEmpty() }
    }

    companion object {
        private val TEST_CLASSES = listOf(
            "AbstractTestClass",
            "ITestClass",
            "TestClass"
        )
    }
}