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
@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION_ERROR", "RedundantVisibilityModifier")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmWasiTargetDsl

@KmpConfigurationDsl
public sealed class TargetWasmContainer<T: KotlinWasmTargetDsl> private constructor(
    targetName: String,
): KmpTarget.NonJvm<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        @ExperimentalWasmDsl
        public fun wasmJs(action: Action<WasmJs>) {
            wasmJs("wasmJs", action)
        }

        @ExperimentalWasmDsl
        public fun wasmJs(targetName: String, action: Action<WasmJs>) {
            val container = holder.find(targetName) ?: WasmJs(targetName)
            action.execute(container)
            holder.add(container)
        }

        @ExperimentalWasmDsl
        public fun wasmWasi(action: Action<WasmWasi>) {
            wasmWasi("wasmWasi", action)
        }

        @ExperimentalWasmDsl
        public fun wasmWasi(targetName: String, action: Action<WasmWasi>) {
            val container = holder.find(targetName) ?: WasmWasi(targetName)
            action.execute(container)
            holder.add(container)
        }


        @ExperimentalWasmDsl
        @Deprecated("use wasmJs instead", level = DeprecationLevel.ERROR)
        public fun wasm(action: Action<Wasm>) {
            wasm("wasm", action)
        }

        @ExperimentalWasmDsl
        @Deprecated("use wasmJs instead", level = DeprecationLevel.ERROR)
        public fun wasm(targetName: String, action: Action<Wasm>) {
            val container = holder.find(targetName) ?: Wasm(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    @Deprecated("use wasmJs instead", level = DeprecationLevel.ERROR)
    public class Wasm internal constructor(
        targetName: String,
    ): TargetWasmContainer<KotlinWasmTargetDsl>(targetName)

    @KmpConfigurationDsl
    public class WasmJs internal constructor(
        targetName: String,
    ): TargetWasmContainer<KotlinWasmJsTargetDsl>(targetName)

    @KmpConfigurationDsl
    public class WasmWasi internal constructor(
        targetName: String,
    ): TargetWasmContainer<KotlinWasmWasiTargetDsl>(targetName)

    @JvmSynthetic
    @OptIn(ExperimentalWasmDsl::class)
    internal override fun setup(project: Project, kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            when (this@TargetWasmContainer) {
                is Wasm -> {
                    wasm(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is WasmJs -> {
                    wasmJs(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is WasmWasi -> {
                    wasmWasi(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
            }

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${NON_JVM}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${NON_JVM}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @get:JvmSynthetic
    internal override val sortOrder: Byte = 12
}
