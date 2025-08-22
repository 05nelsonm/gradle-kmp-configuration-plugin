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
@file:Suppress("DeprecatedCallableAddReplaceWith", "RedundantVisibilityModifier")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import io.matthewnelson.kmp.configuration.extension.container.OptionsContainer.Companion.findNonSimulator
import io.matthewnelson.kmp.configuration.extension.container.OptionsContainer.Companion.findSimulator
import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests

public sealed class TargetIosContainer<T: KotlinNativeTarget> private constructor(
    targetName: String,
): KmpTarget.NonJvm.Native.Unix.Darwin.Ios<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun iosAll() {
            iosArm64()
            iosX64()
            iosSimulatorArm64()
        }

        public fun iosArm64() { iosArm64 {} }

        public fun iosArm64(action: Action<Arm64>) {
            iosArm64("iosArm64", action)
        }

        public fun iosArm64(targetName: String, action: Action<Arm64>) {
            val container = holder.find(targetName) ?: Arm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun iosX64() { iosX64 {} }

        public fun iosX64(action: Action<X64>) {
            iosX64("iosX64", action)
        }

        public fun iosX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun iosSimulatorArm64() { iosSimulatorArm64 {} }

        public fun iosSimulatorArm64(action: Action<SimulatorArm64>) {
            iosSimulatorArm64("iosSimulatorArm64", action)
        }

        public fun iosSimulatorArm64(targetName: String, action: Action<SimulatorArm64>) {
            val container = holder.find(targetName) ?: SimulatorArm64(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class Arm64 internal constructor(
        targetName: String,
    ): TargetIosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String,
    ): TargetIosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class SimulatorArm64 internal constructor(
        targetName: String,
    ): TargetIosContainer<KotlinNativeTargetWithSimulatorTests>(targetName)

    @JvmSynthetic
    internal final override fun setup(project: Project, kotlin: KotlinMultiplatformExtension) {
        @Suppress("RedundantSamConstructor")
        with(kotlin) {
            val isSimulator = when (this@TargetIosContainer) {
                is Arm64 -> {
                    iosArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                    false
                }
                is SimulatorArm64 -> {
                    iosSimulatorArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                    true
                }
                is X64 -> {
                    iosX64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                    true
                }
            }

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    val main = if (!isSimulator) {
                        findNonSimulator(IOS, isMain = true)
                    } else {
                        findSimulator(IOS, isMain = true)
                    } ?: getByName("${IOS}Main")

                    ss.dependsOn(main)
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    val test = if (!isSimulator) {
                        findNonSimulator(IOS, isMain = false)
                    } else {
                        findSimulator(IOS, isMain = false)
                    } ?: getByName("${IOS}Test")

                    ss.dependsOn(test)
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @get:JvmSynthetic
    internal final override val sortOrder: Byte = 31

    internal companion object {
        internal const val IOS = "ios"
    }
}
