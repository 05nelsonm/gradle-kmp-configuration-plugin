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
@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

public sealed class TargetLinuxContainer<T: KotlinNativeTarget> private constructor(
    targetName: String
): KmpTarget.NonJvm.Native.Unix.Linux<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun linuxAll() {
            if (!holder.kotlinPluginVersion.isAtLeast(1, 9, 20)) {
                linuxArm32Hfp()
                linuxMips32()
                linuxMipsel32()
            }
            linuxArm64()
            linuxX64()
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxArm32Hfp() { linuxArm32Hfp {} }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxArm32Hfp(action: Action<Arm32Hfp>) {
            linuxArm32Hfp("linuxArm32Hfp", action)
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxArm32Hfp(targetName: String, action: Action<Arm32Hfp>) {
            val container = holder.find(targetName) ?: Arm32Hfp(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun linuxArm64() { linuxArm64 {} }

        public fun linuxArm64(action: Action<Arm64>) {
            linuxArm64("linuxArm64", action)
        }

        public fun linuxArm64(targetName: String, action: Action<Arm64>) {
            val container = holder.find(targetName) ?: Arm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMips32() { linuxMips32 {} }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMips32(action: Action<Mips32>) {
            linuxMips32("linuxMips32", action)
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMips32(targetName: String, action: Action<Mips32>) {
            val container = holder.find(targetName) ?: Mips32(targetName)
            action.execute(container)
            holder.add(container)
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMipsel32() { linuxMipsel32 {} }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMipsel32(action: Action<Mipsel32>) {
            linuxMipsel32("linuxMipsel32", action)
        }

        @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
        public fun linuxMipsel32(targetName: String, action: Action<Mipsel32>) {
            val container = holder.find(targetName) ?: Mipsel32(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun linuxX64() { linuxX64 {} }

        public fun linuxX64(action: Action<X64>) {
            linuxX64("linuxX64", action)
        }

        public fun linuxX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
    public class Arm32Hfp internal constructor(
        targetName: String
    ): TargetLinuxContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class Arm64 internal constructor(
        targetName: String
    ): TargetLinuxContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
    public class Mips32 internal constructor(
        targetName: String
    ): TargetLinuxContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
    public class Mipsel32 internal constructor(
        targetName: String
    ): TargetLinuxContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String
    ): TargetLinuxContainer<KotlinNativeTarget>(targetName)

    @JvmSynthetic
    internal final override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = when (this@TargetLinuxContainer) {
                is Arm32Hfp -> {
                    linuxArm32Hfp(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is Arm64 -> {
                    linuxArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is Mips32 -> {
                    linuxMips32(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is Mipsel32 -> {
                    linuxMipsel32(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is X64 -> {
                    linuxX64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${LINUX}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${LINUX}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 41

    internal companion object {
        internal const val LINUX = "linux"
    }
}
