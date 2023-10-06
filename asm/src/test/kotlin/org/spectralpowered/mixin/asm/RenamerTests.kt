package org.spectralpowered.mixin.asm

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.mixin.asm.remap.ClassGroupRenamer
import org.spectralpowered.mixin.asm.tree.*
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RenamerTests {

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
    fun `renamer tests`() {
        val renamer = ClassGroupRenamer(group)
        var classCount = 0
        var methodCount = 0
        var fieldCount = 0

        group.classes.forEach { cls ->
            renamer.renameClass(cls, "class${++classCount}")
        }

        group.classes.forEach { cls ->
            cls.methods.forEach methodLoop@ { method ->
                if(method.isConstructor() || method.isInitializer()) return@methodLoop
                renamer.renameMethod(method, "method${++methodCount}")
            }

            cls.fields.forEach fieldLoop@ { field ->
                renamer.renameField(field, "field${++fieldCount}")
            }
        }

        val targMethod = group.getClass("TestClass")!!.getMethod("method", "()V")!!
        val targetField = group.getClass("TestClass")!!.getField("staticField", "Ljava/lang/String;")!!
        val newCls = group.getClass("TestClass2")!!
        renamer.moveMethod(targMethod, newCls)
        renamer.moveField(targetField, newCls)

        renamer.apply()
        assertNotNull(group.getClass("class1"))

        val out = File("tmp.jar")

        group.writeJar(out)
        assertTrue { group.classes.size == 4 }
    }



    companion object {
        private val TEST_CLASSES = listOf(
            "AbstractTestClass",
            "ITestClass",
            "TestClass",
            "TestClass2"
        )
    }
}