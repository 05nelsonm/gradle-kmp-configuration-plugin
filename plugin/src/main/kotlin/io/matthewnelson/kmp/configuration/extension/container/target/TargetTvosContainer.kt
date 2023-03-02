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

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests

public sealed class TargetTvosContainer<T: KotlinNativeTarget> private constructor(
    targetName: String
): KmpTarget.NonJvm.Native.Unix.Darwin.Tvos<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun tvosAll() {
            tvosArm64()
            tvosX64()
            tvosSimulatorArm64()
        }

        public fun tvosArm64() { tvosArm64 {} }

        public fun tvosArm64(action: Action<Arm64>) {
            tvosArm64("tvosArm64", action)
        }

        public fun tvosArm64(targetName: String, action: Action<Arm64>) {
            val container = holder.find(targetName) ?: Arm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun tvosX64() { tvosX64 {} }

        public fun tvosX64(action: Action<X64>) {
            tvosX64("tvosX64", action)
        }

        public fun tvosX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun tvosSimulatorArm64() { tvosSimulatorArm64 {} }

        public fun tvosSimulatorArm64(action: Action<SimulatorArm64>) {
            tvosSimulatorArm64("tvosSimulatorArm64", action)
        }

        public fun tvosSimulatorArm64(targetName: String, action: Action<SimulatorArm64>) {
            val container = holder.find(targetName) ?: SimulatorArm64(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class Arm64 internal constructor(
        targetName: String
    ): TargetTvosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String
    ): TargetTvosContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class SimulatorArm64 internal constructor(
        targetName: String
    ): TargetTvosContainer<KotlinNativeTargetWithSimulatorTests>(targetName)


    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = when (this@TargetTvosContainer) {
                is Arm64 -> {
                    tvosArm64(targetName, Action { t ->
                        lazyTarget?.execute(t)
                    })
                }
                is SimulatorArm64 -> {
                    tvosSimulatorArm64(targetName, Action { t ->
                        lazyTarget?.execute(t)
                    })
                }
                is X64 -> {
                    tvosX64(targetName, Action { t ->
                        lazyTarget?.execute(t)
                    })
                }
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${TVOS}Main"))
                    lazySourceSetMain?.execute(ss)
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${TVOS}Test"))
                    lazySourceSetTest?.execute(ss)
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 33

    internal companion object {
        internal const val TVOS = "tvos"
    }
}
