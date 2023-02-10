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
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmTargetDsl

@KmpConfigurationDsl
public class TargetWasmContainer internal constructor(
    targetName: String,
): KmpTarget.NonJvm<KotlinWasmTargetDsl>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        @ExperimentalWasmDsl
        public fun wasm() {
            wasm { container ->
                container.target { dsl ->
                    dsl.browser {
                        testTask {
                            useMocha { timeout = "30s" }
                        }
                    }
                    dsl.nodejs {
                        testTask {
                            useMocha { timeout = "30s" }
                        }
                    }
                    dsl.d8 {
                        testTask {
                            useMocha { timeout = "30s" }
                        }
                    }
                }
            }
        }

        @ExperimentalWasmDsl
        public fun wasm(action: Action<TargetWasmContainer>) {
            wasm("wasm", action)
        }

        @ExperimentalWasmDsl
        public fun wasm(targetName: String, action: Action<TargetWasmContainer>) {
            val container = holder.find(targetName) ?: TargetWasmContainer(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @JvmSynthetic
    @OptIn(ExperimentalWasmDsl::class)
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            val target = wasm(targetName) t@ {
                lazyTarget?.execute(this@t)
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${NON_JVM}Main"))
                    lazySourceSetMain?.execute(ss)
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${NON_JVM}Test"))
                    lazySourceSetTest?.execute(ss)
                }
            }
        }
    }

    @JvmSynthetic
    internal override val sortOrder: Byte = 12
}
