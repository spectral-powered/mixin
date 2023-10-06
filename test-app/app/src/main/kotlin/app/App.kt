@file:Suppress("UNCHECKED_CAST")

package app

import api.TestApi
import org.spectralpowered.mixin.Injector
import java.io.File
import java.net.URLClassLoader

object App {

    @JvmStatic
    fun main(args: Array<String>) {
        val mixinJar = File("../mixins/build/libs/mixins-1.0.0.jar")
        val mixinApiJar = File("../api/build/libs/api-1.0.0.jar")
        val targetJar = File("../target/build/libs/target-1.0.0.jar")
        val outputJar = File("build/libs/target.injected.jar")

        val injector = Injector(mixinJar, mixinApiJar, targetJar, outputJar)
        injector.inject()

        println("")
        println("")
        println("Starting test class.")
        val classLoader = URLClassLoader(arrayOf(outputJar.toURI().toURL()))
        val testKlass = classLoader.loadClass("target.Test") as Class<TestApi>
        val testInst = testKlass.getDeclaredConstructor().newInstance()
        testInst.start()
    }
}