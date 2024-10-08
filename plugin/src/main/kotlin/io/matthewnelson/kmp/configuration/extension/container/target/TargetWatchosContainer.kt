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
@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION_ERROR")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import io.matthewnelson.kmp.configuration.extension.container.OptionsContainer.Companion.findNonSimulator
import io.matthewnelson.kmp.configuration.extension.container.OptionsContainer.Companion.findSimulator
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.konan.target.DEPRECATED_TARGET_MESSAGE

public sealed class TargetWatchosContainer<T: KotlinNativeTarget> private constructor(
    targetName: String
): KmpTarget.NonJvm.Native.Unix.Darwin.Watchos<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun watchosAll() {
            watchosArm32()
            watchosArm64()
            if (holder.kotlinPluginVersion.isAtLeast(1, 8)) {
                watchosDeviceArm64()
            }
            watchosX64()
            if (!holder.kotlinPluginVersion.isAtLeast(1, 9, 20)) {
                watchosX86()
            }
            watchosSimulatorArm64()
        }

        public fun watchosArm32() { watchosArm32 {} }

        public fun watchosArm32(action: Action<Arm32>) {
            watchosArm32("watchosArm32", action)
        }

        public fun watchosArm32(targetName: String, action: Action<Arm32>) {
            val container = holder.find(targetName) ?: Arm32(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun watchosArm64() { watchosArm64 {} }

        public fun watchosArm64(action: Action<Arm64>) {
            watchosArm64("watchosArm64", action)
        }

        public fun watchosArm64(targetName: String, action: Action<Arm64>) {
            val container = holder.find(targetName) ?: Arm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun watchosDeviceArm64() { watchosDeviceArm64 {} }

        public fun watchosDeviceArm64(action: Action<DeviceArm64>) {
            watchosDeviceArm64("watchosDeviceArm64", action)
        }

        public fun watchosDeviceArm64(targetName: String, action: Action<DeviceArm64>) {
            if (!holder.kotlinPluginVersion.isAtLeast(1, 8)) {
                throw GradleException("watchosDeviceArm64 requires Kotlin 1.8.0 or greater")
            }

            val container = holder.find(targetName) ?: DeviceArm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun watchosX64() { watchosX64 {} }

        public fun watchosX64(action: Action<X64>) {
            watchosX64("watchosX64", action)
        }

        public fun watchosX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun watchosX86() { watchosX86 {} }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun watchosX86(action: Action<X86>) {
            watchosX86("watchosX86", action)
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun watchosX86(targetName: String, action: Action<X86>) {
            val container = holder.find(targetName) ?: X86(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun watchosSimulatorArm64() { watchosSimulatorArm64 {} }

        public fun watchosSimulatorArm64(action: Action<SimulatorArm64>) {
            watchosSimulatorArm64("watchosSimulatorArm64", action)
        }

        public fun watchosSimulatorArm64(targetName: String, action: Action<SimulatorArm64>) {
            val container = holder.find(targetName) ?: SimulatorArm64(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class Arm32 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class Arm64 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class DeviceArm64 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
    public class X86 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class SimulatorArm64 internal constructor(
        targetName: String
    ): TargetWatchosContainer<KotlinNativeTargetWithSimulatorTests>(targetName)


    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        @Suppress("RedundantSamConstructor")
        with(kotlin) {
            val (target, isSimulator) = when (this@TargetWatchosContainer) {
                is Arm32 -> {
                    watchosArm32(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to false
                }
                is Arm64 -> {
                    watchosArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to false
                }
                is DeviceArm64 -> {
                    watchosDeviceArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to false
                }
                is SimulatorArm64 -> {
                    watchosSimulatorArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to true
                }
                is X64 -> {
                    watchosX64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to true
                }
                is X86 -> {
                    watchosX86(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    }) to false
                }
            }

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    val main = if (!isSimulator) {
                        findNonSimulator(WATCHOS, isMain = true)
                    } else {
                        findSimulator(WATCHOS, isMain = true)
                    } ?: getByName("${WATCHOS}Main")

                    ss.dependsOn(main)
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    val test = if (!isSimulator) {
                        findNonSimulator(WATCHOS, isMain = false)
                    } else {
                        findSimulator(WATCHOS, isMain = false)
                    } ?: getByName("${WATCHOS}Test")

                    ss.dependsOn(test)
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 34

    internal companion object {
        internal const val WATCHOS = "watchos"
    }
}
