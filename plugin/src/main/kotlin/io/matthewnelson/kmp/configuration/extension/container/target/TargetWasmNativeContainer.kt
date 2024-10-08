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
@file:Suppress("ClassName", "DeprecatedCallableAddReplaceWith", "DEPRECATION", "DEPRECATION_ERROR")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.DEPRECATED_TARGET_MESSAGE

@Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
public sealed class TargetWasmNativeContainer<T: KotlinNativeTarget> private constructor(
    targetName: String,
): KmpTarget.NonJvm.Native.Wasm<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun wasmNativeAll() {
            wasm32()
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun wasm32() {
            wasm32 {}
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun wasm32(action: Action<_32>) {
            wasm32("wasm32", action)
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun wasm32(targetName: String, action: Action<_32>) {
            val container = holder.find(targetName) ?: _32(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
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
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
            }

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${WASM_NATIVE}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${WASM_NATIVE}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
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
