# gradle-kmp-configuration-plugin
A Gradle Plugin for setting up Kotlin Multiplatform projects.

1. Automatically configures hierarchical source sets.
2. Enables passing of build targets via command line to control what gets 
   configured (great for CI).

## Requirements

Minimum supported versions:
- Gradle: `6.7`
- Kotlin Gradle Plugin: `1.6.0`
- Android Gradle Plugin: `4.1.0`
- Java: `11`

## Hierarchical Source Set Structure

Will automatically configure project with a hierarchical source set structure

```
 common
   |-- jvmAndroid
   |     |-- jvm
   |     '-- android
   '-- nonJvm
         |-- js
         |-- wasm
         |-- wasmJs
         |-- wasmWasi
         '-- native
               |-- androidNative
               |     |-- androidNativeArm32
               |     |-- androidNativeArm64
               |     |-- androidNativeX64
               |     '-- androidNativeX86
               |-- unix
               |     |-- darwin
               |     |     |-- ios
               |     |     |     |-- iosArm32
               |     |     |     |-- iosArm64
               |     |     |     |-- iosX64
               |     |     |     '-- iosSimulatorArm64
               |     |     |-- macos
               |     |     |     |-- macosArm64
               |     |     |     '-- macosX64
               |     |     |-- tvos
               |     |     |     |-- tvosArm64
               |     |     |     |-- tvosX64
               |     |     |     '-- tvosSimulatorArm64
               |     |     '-- watchos
               |     |           |-- watchosArm32
               |     |           |-- watchosArm64
               |     |           |-- watchosDeviceArm64
               |     |           |-- watchosX64
               |     |           |-- watchosX86
               |     |           '-- watchosSimulatorArm64
               |     '-- linux
               |           |-- linuxArm32Hfp
               |           |-- linuxArm64
               |           |-- linuxMips32
               |           |-- linuxMipsel32
               |           '-- linuxX64
               |-- mingw
               |     |-- mingwX64
               |     '-- mingwX86
               '-- wasmNative
                     '-- wasm32
```

## Target Properties

You can control what targets are enabled via passing of properties at build time.

Only enable the `linuxX64` target when building
```bash
./gradlew :samples:native:runDebugExecutableLinuxX64 -PKMP_TARGETS=LINUX_X64
```

Only configure the `jvm` target when building
```bash
./gradlew :samples:javafx:run -PKMP_TARGETS=JVM
```

Override any `KMP_TARGETS` property that may be configured (note the usage of `-D` instead of `-P`)
```bash
./gradlew build -DKMP_TARGETS_ALL
```

This helps with fine-tuning CI builds. For example, if you want to run tests for `Java 11, 16, 17, 18`, 
you can configure things to build all targets for `Java 11`, then pass only `-PKMP_TARGETS=JVM` 
for the `Java 16, 17, 18` builds such that you are not wasting resources by compiling, say, all the 
darwin targets (iOS, macOS, tvOS, watchOS).

List of all `KMP_TARGETS`
```
ANDROID,ANDROID_ARM32,ANDROID_ARM64,ANDROID_X64,ANDROID_X86
JVM
JS
LINUX_ARM64,LINUX_X64
MINGW_X64
IOS_ARM64,IOS_SIMULATOR_ARM64,IOS_X64
MACOS_ARM64,MACOS_X64
TVOS_ARM64,TVOS_SIMULATOR_ARM64,TVOS_X64
WATCHOS_ARM32,WATCHOS_ARM64,WATCHOS_DEVICE_ARM64,WATCHOS_SIMULATOR_ARM64,WATCHOS_X64
WASM_JS,WASM_WASI,WASM

// DEPRECATED as of 0.1.5 (based on Kotlin 1.9.20)
LINUX_ARM32HFP,LINUX_MIPS32,LINUX_MIPSEL32
MINGW_X86
IOS_ARM32
WATCHOS_X86
WASM_32
```

Example usage (comma separated list)
```bash
./gradlew build -PKMP_TARGETS="JVM,JS,ANDROID,MACOS_ARM64,MACOS_X64,WASM_JS,WASM_WASI"
```

This is useful in projects that contain multiple modules which support different targets.

 - Module A: `jvm`, `js`
 - Module B: `js`
 - Module C: `jvm`

Module B depends on A  
Module C depends on A  

Passing `-PKMP_TARGETS=JVM` when building means no targets are enabled for Module B. In 
this event, the Kotlin Multiplatform plugin will not be applied and nothing will be 
configured (i.e. the build will not fail due to kotlin multiplatform plugin requiring 
at least 1 target being enabled).

## Extension Usage

For composite builds, you can reference the following examples:
 - [PR #19][pr-19]
 - The [encoding][url-encoding] project

```kotlin
kmpConfiguration {
    configure {
        jvm {
            target {
                
            }
            sourceSetMain {
                dependencies {
                    // Jvm only dependencies
                }
            }
            sourceSetTest {
                // ...
            }

            // Will automatically set
            // KotlinJvmCompilation.kotlinOptions.jvmTarget
            kotlinJvmTarget = JavaVersion.VERSION_1_8
            
            // If you aren't using android and have elected for `withJava()` 
            // in the `target` block above setting this will automatically
            // configure for you:
            //
            // JavaPluginExtension.sourceCompatibility
            // JavaPluginExtension.targetCompatibility
            compileSourceCompatibility = JavaVersion.VERSION_1_8
            compileTargetCompatibility = JavaVersion.VERSION_1_8
        }

        // Android.
        // Note that only 1 android example below will be utilized b/c
        // you can only have 1 android target in a multiplatform module.

        // The `com.android.application` plugin will be applied automatically 
        // if this target is enabled.
        androidApp {
            // Plugins to be applied if this target is being configured. In other
            // words, these plugins will not be applied in the event KMP_TARGETS
            // is passed via command line and does not contain ANDROID
            //
            // `pluginIds` are available for all targets for selectively applying
            // them based on needs
            pluginIds("org.jetbrains.kotlin.kapt", "androidx.navigation.safeargs")

            target {
                // ..
            }
            android {
                // configure BaseAppModuleExtension
            }

            // See jvm lambda above for explanation, works the same
            kotlinJvmTarget = JavaVersion.VERSION_1_8
            compileSourceCompatibility = JavaVersion.VERSION_1_8
            compileTargetCompatibility = JavaVersion.VERSION_1_8
        }

        // Will automatically apply the `com.android.library` plugin
        // if this target is enabled.
        androidLibrary {
            target {
                publishLibraryVariant("release")
            }
            android {
                // configure LibraryExtension
            }
            
            // Additional sourceSet lambda for androidLibrary and androidApp
            sourceSetTestInstrumented {
                // ...
            }
        }

        // The `*All` shortcuts enable all targets for that platform type.
        //
        // NOTE: If more targets become available in future Kotlin
        // releases, this will automatically add them. If that behavior
        // is undesired, consider not utilizing the `*All` shortcuts.
        iosAll()
        macosAll()
        tvosAll()
        watchosAll()

        options {
            // Will create additional source sets for `iOS`, `tvOS`, and
            // `watchOS` simulator targets to inherit from
            //
            // e.g. iosSimulator{Main/Test}
            useSimulatorSourceSets = true
            
            // Will create additional source sets for `iOS`, `tvOS`, and
            // `watchOS` NON-simulator targets to inherit from
            //
            // e.g. iosNonSimulator{Main/Test}
            useNonSimulatorSourceSets = true
        }

        androidNativeAll()
        linuxAll()

        // All androidNative targets are enabled from above, but say you only
        // need to configure the X64 variant. This will return the same lazy
        // container that was created in `androidNativeAll` above so you can
        // do that.
        androidNativeX64 {
            target {
                // ...
            }
        }
        
        // Add another with a non-default name
        androidNativeX64("androidNativeX64Awesome") {
            // ...
        }

        js()
        wasmJs {
            // ...
        }
        wasmWasi {
            // ..
        }
        
        // The `common` block is for configuring common source sets.
        // This will only be invoked if there is at least 1 target
        // being configured.
        common {
            pluginIds("kotlinx-atomicfu")

            sourceSetMain {
                dependencies {
                    // ...
                }
            }
            sourceSetTest {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }

        // This will only be invoked if there is at least 1 target
        // being configured.
        kotlin {
            // Additional configuration via the KotlinMultiplatformExtension
            
            with(sourcSets) {
                // Want to use `findByName` in the event KMP_TARGETS is
                // set and does not include ANDROID or JVM, which means
                // the `jvmAndroid` intermediate source set will not be
                // created.
                val jvmAndroidMain: KotlinSourceSet? = findByName("jvmAndroidMain")?.apply {

                }

                // More intermediate source sets, as depicted above in
                // section `Hierarchical Source Set Structure`
                val nativeMain: KotlinSourceSet? = findByName("nativeMain")?.apply {

                }
                val darwinMain: KotlinSourceSet? = findByName("darwinMain")?.apply {

                }
                val iosMain: KotlinSourceSet? = findByName("iosMain")?.apply {

                }

                // setup additional source set configurations
                val linuxMain = findByName("linuxMain")
                val androidNativeMain = findByName("androidNativeMain")

                if (linuxMain != null || androidNativeMain != null) {
                    val linuxAndroidMain = maybeCreate("linuxAndroidMain").apply {
                        // `linux` and `androidNative` intermediate source sets
                        // inherit from native, so it will always be available
                        // if either of them are configured. So, we can use
                        // `getByName` safely.
                        dependsOn(getByName("nativeMain"))
                    }
                    val linuxAndroidTest = maybeCreate("linuxAndroidTest").apply {
                        dependsOn(getByName("nativeTest"))
                    }

                    linuxMain?.apply { dependsOn(linuxAndroidMain) }
                    findByName("linuxTest")?.apply { dependsOn(linuxAndroidTest) }

                    androidNativeMain?.apply { dependsOn(linuxAndroidMain) }
                    findByName("androidNativeTest")?.apply { dependsOn(linuxAndroidTest) }
                }
            }
        }
    }
}
```

## Gradle

**Using the `plugins` block**
<details open>
    <summary>Kotlin</summary>

<!-- TAG_VERSION -->

```kotlin
plugins {
    // If you are using androidApp (as depicted in the above example)
    id("com.android.application") version("x.x.x") apply(false)

    // If you are using androidLibrary (as depicted in the above example)
    id("com.android.library") version("x.x.x") apply(false)

    id("org.jetbrains.kotlin.multiplatform") version("x.x.x") apply(false)
    id("io.matthewnelson.kmp.configuration") version("0.1.6")
}
```

</details>

<details>
    <summary>Groovy</summary>

<!-- TAG_VERSION -->

```groovy
plugins {
    // If you are using androidApp (as depicted in the above example)
    id 'com.android.application' version 'x.x.x' apply false

    // If you are using androidLibrary (as depicted in the above example)
    id 'com.android.library' version 'x.x.x' apply false

    id 'org.jetbrains.kotlin.multiplatform' version 'x.x.x' apply false
    id 'io.matthewnelson.kmp.configuration' version '0.1.6'
}
```

</details>

**Using the `apply plugin` (the old way)**
<details open>
    <summary>Kotlin</summary>

<!-- TAG_VERSION -->

top-level build file:
```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
    
    dependencies {
        // kotlin gradle
        // android gradle (if you have an android target)
        classpath("io.matthewnelson:gradle-kmp-configuration-plugin:0.1.6")
    }
}
```

project module:
```kotlin
plugins {
    id("io.matthewnelson.kmp.configuration")
}

kmpConfiguration {
    configure {
        // ...
    }
}
```

</details>

<details>
    <summary>Groovy</summary>

<!-- TAG_VERSION -->

top-level build file:
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.matthewnelson:gradle-kmp-configuration-plugin:0.1.6'
    }
}
```

project module:
```groovy
plugins {
    id 'io.matthewnelson.kmp.configuration'
}

kmpConfiguration {
    configure {
        // ...
    }
}
```

</details>

[pr-19]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/19
[url-encoding]: https://github.com/05nelsonm/encoding
