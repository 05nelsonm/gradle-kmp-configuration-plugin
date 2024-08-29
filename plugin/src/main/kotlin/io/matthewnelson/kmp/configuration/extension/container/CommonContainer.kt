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

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@KmpConfigurationDsl
public class CommonContainer internal constructor(): Container.ConfigurableTarget() {

    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin.sourceSets) {
            val commonMain = getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
            lazySourceSetMain.forEach { action -> action.execute(commonMain) }

            val commonTest = getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)
            lazySourceSetTest.forEach { action -> action.execute(commonTest) }
        }
    }

    @JvmSynthetic
    internal override val sortOrder: Byte = (Byte.MAX_VALUE - 1).toByte()
    override fun hashCode(): Int = 17 * 31 + this::class.java.name.hashCode()
    override fun equals(other: Any?): Boolean = other is CommonContainer
}
