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
import io.matthewnelson.kmp.configuration.extension.container.CommonContainer
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import io.matthewnelson.kmp.configuration.extension.container.OptionsContainer
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@KmpConfigurationDsl
public class KmpConfigurationContainerDsl private constructor(
    override val holder: ContainerHolder
) : TargetAndroidContainer.Configure,
    TargetAndroidNativeContainer.Configure,
    TargetIosContainer.Configure,
    TargetJsContainer.Configure,
    TargetJvmContainer.Configure,
    TargetLinuxContainer.Configure,
    TargetMacosContainer.Configure,
    TargetMingwContainer.Configure,
    TargetTvosContainer.Configure,
    TargetWatchosContainer.Configure,
    TargetWasmContainer.Configure,
    @Suppress("DEPRECATION_ERROR")
    TargetWasmNativeContainer.Configure
{

    public fun options(action: Action<OptionsContainer>) {
        val container = holder.getOrCreateOptionsContainer()
        action.execute(container)
    }

    public fun common(action: Action<CommonContainer>) {
        val container = holder.getOrCreateCommonContainer()
        action.execute(container)
    }

    @KmpConfigurationDsl
    public fun kotlin(action: Action<KotlinMultiplatformExtension>) {
        val container = holder.getOrCreateKotlinContainer()
        container.kotlin(action)
    }

    internal companion object {
        @JvmSynthetic
        internal fun instance(holder: ContainerHolder) = KmpConfigurationContainerDsl(holder)
    }
}
