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
package io.matthewnelson.kmp.configuration.extension.container

import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

public sealed class Container {

    @get:JvmSynthetic
    internal abstract val sortOrder: Byte

    @JvmSynthetic
    internal abstract fun setup(kotlin: KotlinMultiplatformExtension)

    public abstract class ConfigurableTarget internal constructor(): Container() {

        private val pluginIds: MutableSet<String> = mutableSetOf()
        public fun pluginIds(vararg ids: String) { pluginIds += ids.toSet() }

        protected var lazySourceSetMain: Action<KotlinSourceSet>? = null
            private set
        public fun sourceSetMain(action: Action<KotlinSourceSet>) { lazySourceSetMain = action }

        protected var lazySourceSetTest: Action<KotlinSourceSet>? = null
            private set
        public fun sourceSetTest(action: Action<KotlinSourceSet>) { lazySourceSetTest = action }

        protected fun applyPlugins(project: Project) {
            for (id in pluginIds) {
                project.plugins.apply(id)
            }
        }
    }
}
