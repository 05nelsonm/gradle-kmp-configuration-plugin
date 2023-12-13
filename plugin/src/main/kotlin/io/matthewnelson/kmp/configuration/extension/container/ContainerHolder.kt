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

import io.matthewnelson.kmp.configuration.extension.container.target.*
import io.matthewnelson.kmp.configuration.extension.container.target.KmpTargetProperty.Companion.property

public class ContainerHolder private constructor(
    internal val kotlinPluginVersion: KotlinVersion,
    private val containers: MutableSet<Container>,
    private val isKmpTargetsAllSet: Boolean,
    private val kmpTargetsProperty: Set<KmpTargetProperty>?,
) {

    @JvmSynthetic
    internal fun add(container: Container) {
        if (container !is KmpTarget<*>) {
            containers.add(container)
            return
        }

        if (isKmpTargetsAllSet || kmpTargetsProperty == null) {
            containers.add(container)
            return
        }

        if (kmpTargetsProperty.contains(container.property())) {
            containers.add(container)
        }
    }

    @JvmSynthetic
    internal fun getOrCreateOptionsContainer(): OptionsContainer {
        return containers
            .filterIsInstance<OptionsContainer>()
            .firstOrNull()
            ?: OptionsContainer().also { add(it) }
    }

    @JvmSynthetic
    internal fun getOrCreateCommonContainer(): CommonContainer {
        return containers
            .filterIsInstance<CommonContainer>()
            .firstOrNull()
            ?: CommonContainer().also { add(it) }
    }

    @JvmSynthetic
    internal fun getOrCreateKotlinContainer(): KotlinExtensionActionContainer {
        return containers
            .filterIsInstance<KotlinExtensionActionContainer>()
            .firstOrNull()
            ?: KotlinExtensionActionContainer().also { add(it) }
    }

    @JvmSynthetic
    internal inline fun <reified T: KmpTarget<*>> find(targetName: String): T? {
        return containers.filterIsInstance<T>().find { targetName == it.targetName }
    }

    internal companion object {
        @JvmSynthetic
        internal fun instance(
            kotlinPluginVersion: KotlinVersion,
            containers: MutableSet<Container>,
            isKmpTargetsAllSet: Boolean,
            kmpTargetsProperty: Set<KmpTargetProperty>?,
        ): ContainerHolder {
            return ContainerHolder(
                kotlinPluginVersion,
                containers,
                isKmpTargetsAllSet,
                kmpTargetsProperty
            )
        }
    }
}
