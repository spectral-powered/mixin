package app

import org.spectralpowered.mixin.Injector
import java.net.URLClassLoader

object App {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting App")
        val targetJar = App::class.java.getResource("/target.injected.jar")!!.toURI().toURL()
        val classLoader = URLClassLoader(arrayOf(targetJar))
        val testCls = classLoader.loadClass("target.Test")
        testCls.getMethod("start").invoke(null)
    }
}