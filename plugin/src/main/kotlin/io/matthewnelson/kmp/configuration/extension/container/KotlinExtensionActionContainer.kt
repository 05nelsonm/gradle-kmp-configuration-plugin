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
@file:Suppress("RedundantVisibilityModifier")

package io.matthewnelson.kmp.configuration.extension.container

import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class KotlinExtensionActionContainer internal constructor(): Container() {

    private val lazyKotlin = mutableListOf<Action<KotlinMultiplatformExtension>>()

    @JvmSynthetic
    internal fun kotlin(action: Action<KotlinMultiplatformExtension>) { lazyKotlin.add(action) }

    @JvmSynthetic
    internal override fun setup(project: Project, kotlin: KotlinMultiplatformExtension) {
        lazyKotlin.forEach { action -> action.execute(kotlin) }
    }

    @get:JvmSynthetic
    internal override val sortOrder: Byte = Byte.MAX_VALUE
    public override fun hashCode(): Int = 17 * 31 + this::class.java.name.hashCode()
    public override fun equals(other: Any?): Boolean = other is KotlinExtensionActionContainer
}
