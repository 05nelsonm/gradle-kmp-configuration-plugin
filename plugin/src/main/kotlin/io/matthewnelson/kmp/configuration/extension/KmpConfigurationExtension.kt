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

package io.matthewnelson.kmp.configuration.extension

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.Container
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import io.matthewnelson.kmp.configuration.extension.container.target.KmpConfigurationContainerDsl
import io.matthewnelson.kmp.configuration.extension.container.target.KmpTargetProperty
import org.gradle.api.Action
import org.gradle.api.GradleException
import javax.inject.Inject

@KmpConfigurationDsl
public abstract class KmpConfigurationExtension @Inject internal constructor(
    private val isKmpTargetsAllSet: Boolean,
    private val kmpTargetsProperty: Set<KmpTargetProperty>?,
    private val configureContainers: Action<Set<Container>>,
) {

    @Volatile
    private var isConfigured = false

    public fun configure(action: Action<KmpConfigurationContainerDsl>) {
        synchronized(this) {
            if (isConfigured) throw GradleException("$NAME.configure can only be invoked once")
            isConfigured = true

            val containers = mutableSetOf<Container>()
            val holder = ContainerHolder.instance(containers, isKmpTargetsAllSet, kmpTargetsProperty)
            action.execute(KmpConfigurationContainerDsl.instance(holder))
            configureContainers.execute(containers.sortedBy { it.sortOrder }.toSet())
        }
    }

    internal companion object {
        internal const val NAME = "kmpConfiguration"
    }
}
