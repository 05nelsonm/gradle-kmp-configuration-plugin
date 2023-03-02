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
@file:Suppress("ClassName")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

public sealed class TargetWasmNativeContainer<T: KotlinNativeTarget> private constructor(
    targetName: String,
): KmpTarget.NonJvm.Native.Wasm<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun wasmNativeAll() {
            wasm32()
        }

        public fun wasm32() { wasm32 {} }

        public fun wasm32(action: Action<_32>) {
            wasm32("wasm32", action)
        }

        public fun wasm32(targetName: String, action: Action<_32>) {
            val container = holder.find(targetName) ?: _32(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class _32 internal constructor(
        targetName: String,
    ): TargetWasmNativeContainer<KotlinNativeTarget>(targetName)

    @JvmSynthetic
    internal final override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = when (this@TargetWasmNativeContainer) {
                is _32 -> {
                    wasm32(targetName, Action { t ->
                        lazyTarget?.execute(t)
                    })
                }
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${WASM_NATIVE}Main"))
                    lazySourceSetMain?.execute(ss)
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${WASM_NATIVE}Test"))
                    lazySourceSetTest?.execute(ss)
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 61

    internal companion object {
        internal const val WASM_NATIVE = "wasmNative"
    }
}
