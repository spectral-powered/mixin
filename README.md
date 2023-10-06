<h1 align="center">
  <a href="https://github.com/spectral-powered/mixin">
    <img src="https://raw.githubusercontent.com/spectral-powered/mixin/master/asset/project-logo.png" alt="mixin">
  </a>
</h1>
<p align="center">
</p>
<p align="center">
  Spectral Powered Mixin contains a Mixin style injector framework in order to modify bytecode in a scalable way.
  Mixin also provides a simple injector setup solution via a gradle plugin handling the build-time injection and configurations.
</p>

<p align="center">
<a href="#introduction">Introduction</a> &nbsp;&bull;&nbsp;
<a href="#installation">Installation</a> &nbsp;&bull;&nbsp;
<a href="#usage">Usage</a> &nbsp;&bull;&nbsp;
<a href="#documentation">Documentation</a> &nbsp;&bull;&nbsp;
<a href="#issue">Issue?</a>
</p>

# Introduction
This <b>mixin</b> framework library provides> simple APIs/functions/methods to handle a mixin based approach to bytecode modifications.

- Annotation based raw injections
- Runtime / Compile Time injectors
- Simple gradle plugin configuration
- Easy Api injection
- Smart multi Mixin class merging

## Installation
##### Gradle
Add to `settings.gradle`
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.spectralpowered.org/")
    }
}
```

Add to `build.gradle`
```kotlin
plugins {
    id("org.spectralpowered.mixin.plugin") version "0.1.0-pre1"
}

repositories {
    mavenCentral()
    maven(url = "https://maven.spectralpowered.org/")
}
```

To setup the mixin injector configurations.
```kotlin
dependencies {
    // The project / dependency containing your mixins
    mixin(project(":my-mixin-module"))
    
    // The project / dependency containing your minxin api
    mixinApi(project(":my-api"))
    
    // The project / dependency you want to inject into
    inject(project(":my-injection-target"))
}
```

Mixin dependencies:
```kotlin
dependencies {
    // Includes Everything Normally Needed
    implementation("org.spectralpowered:mixin:0.1.0-pre1")
    
    // Explicit Mixin modules 
    implementation("org.spectralpowered:mixin-injector:0.1.0-pre1")
    implementation("org.spectralpowered:mixin-annotations:0.1.0-pre1")
    implementation("org.spectralpowered:mixin-asm:0.1.0-pre1")
}
```

## Usage
Mixin Class example:
```java
@Mixin(Test.class)
public abstract class TestMixin implements TestApi {
    
    @Shadow
    private abstract void shadow$testMethod();
    
    private void myCustomLogic() {
        System.out.println("Hello from TestMixin!");
    }
    
    @Overwrite
    @Override
    public void testMethod() {
        this.myCustomLogic();
        this.shadow$testMethod();
    }
}
```

Target Class Example:
```java
public class Test {
    
    public void testMethod() {
        System.out.println("Hello from original Test class.");
    }
}
```

Api Class Example:
```java
interface TestApi {
    
    void testMethod();
    
}
```

When you build or run the module/project with the mixin gradle plugin applied. There will be an embedded jar file put in the
resources of your sourceSet at compileTime containing the injected target jar.
```
my-app/
├─ org.myapp.app/
│  ├─ App.class
│  ├─ Other.class
├─ target.injected.jar
```

This injected jar `target.injected.jar` can simply be loaded into a classloader. See example below.
```kotlin
object App {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting app.")
        
        val classLoader = URLClassLoader(arrayOf(App::class.java.getResource("/target.injected.jar")!!.toURI().toURL()))
        val testKlass = classLoader.loadClass("myapp.TestClass") as Class<TestApi>
        val testInstance = testKlass.getDeclaredConstructor().newInstance()
        testInstance.testMethod()
    }
}
```

The following main function will print the following to console:
```
Starting app.
Hello from TestMixin!
Hello from original Test class.
```