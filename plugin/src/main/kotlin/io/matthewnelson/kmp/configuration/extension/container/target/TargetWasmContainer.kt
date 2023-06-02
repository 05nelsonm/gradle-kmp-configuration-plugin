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
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmTargetDsl

@KmpConfigurationDsl
public class TargetWasmContainer internal constructor(
    targetName: String,
): KmpTarget.NonJvm<KotlinWasmTargetDsl>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

//        @ExperimentalWasmDsl
//        public fun wasm() {
//            wasm { container ->
//                @Suppress("RedundantSamConstructor")
//                container.target { dsl ->
//                    dsl.browser( Action { bDsl ->
//                        bDsl.testTask( Action { tDsl ->
//                            tDsl.useMocha( Action { it.timeout = "30s" } )
//                        })
//                    })
//                    dsl.nodejs( Action { nDsl ->
//                        nDsl.testTask( Action { tDsl ->
//                            tDsl.useMocha( Action { it.timeout = "30s" } )
//                        })
//                    })
//                    dsl.d8( Action { dDsl ->
//                        dDsl.testTask( Action { tDsl ->
//                            tDsl.useMocha( Action { it.timeout = "30s" } )
//                        })
//                    })
//                }
//            }
//        }

        @ExperimentalWasmDsl
        public fun wasm(action: Action<TargetWasmContainer>) {
            wasm("wasm", action)
        }

        @ExperimentalWasmDsl
        public fun wasm(targetName: String, action: Action<TargetWasmContainer>) {
            if (!holder.kotlinPluginVersion.isAtLeast(1, 7, 20)) {
                throw GradleException("wasm requires Kotlin 1.7.20 or greater")
            }

            val container = holder.find(targetName) ?: TargetWasmContainer(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @JvmSynthetic
    @OptIn(ExperimentalWasmDsl::class)
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = wasm(targetName, Action { t ->
                lazyTarget.forEach { action -> action.execute(t) }
            })

            applyPlugins(target.project)

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

    @JvmSynthetic
    internal override val sortOrder: Byte = 12
}
