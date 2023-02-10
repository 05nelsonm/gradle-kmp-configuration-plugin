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
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

@KmpConfigurationDsl
public class TargetJvmContainer internal constructor(
    targetName: String
): KmpTarget.Jvm<KotlinJvmTarget>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun jvm() { jvm {} }

        public fun jvm(action: Action<TargetJvmContainer>) {
            jvm("jvm", action)
        }

        public fun jvm(targetName: String, action: Action<TargetJvmContainer>) {
            val container = holder.find(targetName) ?: TargetJvmContainer(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            val target = jvm(targetName) t@ {
                kotlinJvmTarget?.let { version ->
                    compilations.all {
                        it.kotlinOptions.jvmTarget = version.toString()
                    }
                }

                lazyTarget?.execute(this@t)

                if (!withJavaEnabled) return@t

                val sCompatibility = compileSourceCompatibility
                val tCompatibility = compileTargetCompatibility

                if (sCompatibility == null && tCompatibility == null) return@t

                project.extensions.configure(JavaPluginExtension::class.java) { extension ->
                    if (sCompatibility != null) {
                        extension.sourceCompatibility = sCompatibility
                    }
                    if (tCompatibility != null) {
                        extension.targetCompatibility = tCompatibility
                    }
                }
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${JVM_ANDROID}Main"))
                    lazySourceSetMain?.execute(ss)
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${JVM_ANDROID}Test"))
                    lazySourceSetTest?.execute(ss)
                }
            }
        }
    }

    @JvmSynthetic
    internal override val sortOrder: Byte = 2
}
