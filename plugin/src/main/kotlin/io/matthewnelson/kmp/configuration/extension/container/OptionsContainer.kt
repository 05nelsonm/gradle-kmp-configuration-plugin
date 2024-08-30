/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.matthewnelson.kmp.configuration.extension.container

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.target.TargetIosContainer
import io.matthewnelson.kmp.configuration.extension.container.target.TargetTvosContainer
import io.matthewnelson.kmp.configuration.extension.container.target.TargetWatchosContainer
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile

@KmpConfigurationDsl
public class OptionsContainer internal constructor(): Container() {

    /**
     * Setting to `true` will ensure that all targets utilize truly unique
     * `moduleName` parameters for metadata and Jvm/Android compilerOptions.
     *
     * See [KT-69701](https://youtrack.jetbrains.com/issue/KT-69701/)
     * */
    @JvmField
    public var useUniqueModuleNames: Boolean = false

    /**
     * Setting to `true` will create an additional intermediary source
     * set for `iOS`, `tvOS`, and `watchOS` targets that non-simulator
     * targets will derive a root from.
     *
     * e.g. (`iOS`)
     *
     *     darwin
     *       |--- ios
     *       |     |--- iosNonSimulator
     *       |     |     '--- iosArm64
     *       |     |--- iosX64
     *       |     |--- iosSimulatorArm64
     * */
    @JvmField
    public var useNonSimulatorSourceSets: Boolean = false

    /**
     * Setting to `true` will create an additional intermediary source
     * set for `iOS`, `tvOS`, and `watchOS` targets that simulator
     * targets will derive a root from.
     *
     * e.g. (`iOS`)
     *
     *     darwin
     *       |--- ios
     *       |     |--- iosArm64
     *       |     |--- iosSimulator
     *       |     |     |--- iosX64
     *       |     |     '--- iosSimulatorArm64
     * */
    @JvmField
    public var useSimulatorSourceSets: Boolean = false

    override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin.sourceSets) {
            setupNonSimulator(TargetIosContainer.IOS)
            setupNonSimulator(TargetTvosContainer.TVOS)
            setupNonSimulator(TargetWatchosContainer.WATCHOS)
            setupSimulator(TargetIosContainer.IOS)
            setupSimulator(TargetTvosContainer.TVOS)
            setupSimulator(TargetWatchosContainer.WATCHOS)
        }
    }

    @JvmSynthetic
    internal fun setupLast(kotlin: KotlinMultiplatformExtension) {
        kotlin.setupUniqueModuleNames()
    }

    private fun NamedDomainObjectContainer<KotlinSourceSet>.setupNonSimulator(name: String) {
        if (!useNonSimulatorSourceSets) return
        val main = findByName("${name}Main")
        val test = findByName("${name}Test")

        if (main == null || test == null) return
        maybeCreate("${name}NonSimulatorMain").dependsOn(main)
        maybeCreate("${name}NonSimulatorTest").dependsOn(test)
    }

    private fun NamedDomainObjectContainer<KotlinSourceSet>.setupSimulator(name: String) {
        if (!useSimulatorSourceSets) return
        val main = findByName("${name}Main")
        val test = findByName("${name}Test")

        if (main == null || test == null) return
        maybeCreate("${name}SimulatorMain").dependsOn(main)
        maybeCreate("${name}SimulatorTest").dependsOn(test)
    }

    @Suppress("RedundantSamConstructor")
    private fun KotlinMultiplatformExtension.setupUniqueModuleNames() {
        if (!useUniqueModuleNames) return
        val prefix = targets.first().project.let { p -> "${p.group}:${p.name}" }

        metadata(Action { target ->
            target.compilations.all(Action { compilation ->
                val compilationName = compilation.name
                compilation.compileTaskProvider.configure(Action { task ->
                    if (task is BaseKotlinCompile) {
                        task.moduleName.set("${prefix}_${compilationName}")
                    }
                })
            })
        })

        targets.forEach { target ->
            val moduleName = "${prefix}_${target.targetName}"

            when (target) {
                is KotlinAndroidTarget -> {
                    target.compilations.all(Action { compilation ->
                        compilation.compileTaskProvider.configure(Action { task ->
                            task.compilerOptions.moduleName.set(moduleName)
                        })
                    })
                }
                is KotlinJvmTarget -> {
                    target.compilations.all(Action { compilation ->
                        compilation.compileTaskProvider.configure(Action { task ->
                            task.compilerOptions.moduleName.set(moduleName)
                        })
                    })
                }
            }
        }
    }

    override val sortOrder: Byte = Byte.MIN_VALUE

    internal companion object {

        @JvmStatic
        @JvmSynthetic
        internal fun NamedDomainObjectContainer<KotlinSourceSet>.findNonSimulator(
            name: String,
            isMain: Boolean
        ): KotlinSourceSet? {
            val suffix = if (isMain) "Main" else "Test"
            return findByName("${name}NonSimulator$suffix")
        }

        @JvmStatic
        @JvmSynthetic
        internal fun NamedDomainObjectContainer<KotlinSourceSet>.findSimulator(
            name: String,
            isMain: Boolean
        ): KotlinSourceSet? {
            val suffix = if (isMain) "Main" else "Test"
            return findByName("${name}Simulator$suffix")
        }
    }

    @Deprecated(
        "use 'useNonSimulatorSourceSets'",
        ReplaceWith("useNonSimulatorSourceSets")
    )
    public var nonSimulatorSourceSets: Boolean
        set(value) { useNonSimulatorSourceSets = value }
        get() = useNonSimulatorSourceSets
}
