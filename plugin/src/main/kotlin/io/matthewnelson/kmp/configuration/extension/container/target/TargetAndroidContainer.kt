/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.matthewnelson.kmp.configuration.extension.container.target

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

public sealed class TargetAndroidContainer<T: TestedExtension> private constructor(
    targetName: String,
    private val kotlinPluginVersion: KotlinVersion,
): KmpTarget.Jvm<KotlinAndroidTarget>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun androidApp(action: Action<App>) {
            androidApp("android", action)
        }

        public fun androidApp(targetName: String, action: Action<App>) {
            val container = holder.find(targetName) ?: App(targetName, holder.kotlinPluginVersion)
            action.execute(container)
            holder.add(container)
        }

        public fun androidLibrary(action: Action<Library>) {
            androidLibrary("android", action)
        }

        public fun androidLibrary(targetName: String, action: Action<Library>) {
            val container = holder.find(targetName) ?: Library(targetName, holder.kotlinPluginVersion)
            action.execute(container)
            holder.add(container)
        }
    }

    protected val lazyAndroid: MutableList<Action<T>> = mutableListOf()
    public fun android(action: Action<T>) { lazyAndroid.add(action) }

    private val lazySourceSetTestInstrumented: MutableList<Action<KotlinSourceSet>> = mutableListOf()
    public fun sourceSetTestInstrumented(action: Action<KotlinSourceSet>) { lazySourceSetTestInstrumented.add(action) }

    @KmpConfigurationDsl
    public class App internal constructor(
        targetName: String,
        kotlinPluginVersion: KotlinVersion,
    ): TargetAndroidContainer<BaseAppModuleExtension>(targetName, kotlinPluginVersion) {

        protected override fun setupAndroid(project: Project) {
            project.extensions.configure(BaseAppModuleExtension::class.java) { extension ->
                // Set before executing action so that they may be
                // overridden if desired
                extension.compileOptions {
                    compileSourceCompatibility?.let { compatibility ->
                        sourceCompatibility = compatibility
                    }
                    compileTargetCompatibility?.let { compatibility ->
                        targetCompatibility = compatibility
                    }
                }

                lazyAndroid.forEach { action -> action.execute(extension) }
            }
        }
    }

    @KmpConfigurationDsl
    public class Library internal constructor(
        targetName: String,
        kotlinPluginVersion: KotlinVersion,
    ): TargetAndroidContainer<LibraryExtension>(targetName, kotlinPluginVersion) {

        protected override fun setupAndroid(project: Project) {
            project.extensions.configure(LibraryExtension::class.java) { extension ->
                // Set before executing action so that they may be
                // overridden if desired
                extension.compileOptions {
                    compileSourceCompatibility?.let { compatibility ->
                        sourceCompatibility = compatibility
                    }
                    compileTargetCompatibility?.let { compatibility ->
                        targetCompatibility = compatibility
                    }
                }

                lazyAndroid.forEach { action -> action.execute(extension) }
            }
        }
    }

    protected abstract fun setupAndroid(project: Project)

    @JvmSynthetic
    internal final override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = android(targetName, Action { t ->
                kotlinJvmTarget?.let { version ->
                    t.compilations.all {
                        it.kotlinOptions.jvmTarget = version.toString()
                    }
                }

                lazyTarget.forEach { action -> action.execute(t) }
            })

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${JVM_ANDROID}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }

                val layoutVersion = if (kotlinPluginVersion.isAtLeast(1, 8)) {
                    target.project.extraProperties
                        .properties["kotlin.mpp.androidSourceSetLayoutVersion"]
                        ?.toString()
                        ?.toIntOrNull()
                } else {
                    null
                }

                val (test, instrumented) = when (layoutVersion) {
                    2 -> {
                        Pair("androidUnitTest", "androidInstrumentedTest")
                    }
                    else -> {
                        Pair("androidTest", "androidAndroidTest")
                    }
                }

                val jvmAndroidTest = getByName("${JVM_ANDROID}Test")

                getByName(test) { ss ->
                    ss.dependsOn(jvmAndroidTest)
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
                getByName(instrumented) { ss ->
                    ss.dependsOn(jvmAndroidTest)
                    lazySourceSetTestInstrumented.forEach { action -> action.execute(ss) }
                }
            }

            setupAndroid(target.project)
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 1
    final override fun equals(other: Any?): Boolean = other is TargetAndroidContainer<*>
    final override fun hashCode(): Int = 17 * 31 + TargetAndroidContainer::class.java.name.hashCode()
}
