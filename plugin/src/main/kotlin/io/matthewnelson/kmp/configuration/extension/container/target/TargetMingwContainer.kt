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
@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION_ERROR")

package io.matthewnelson.kmp.configuration.extension.container.target

import io.matthewnelson.kmp.configuration.KmpConfigurationDsl
import io.matthewnelson.kmp.configuration.extension.container.ContainerHolder
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.DEPRECATED_TARGET_MESSAGE

public sealed class TargetMingwContainer<T: KotlinNativeTarget> private constructor(
    targetName: String
): KmpTarget.NonJvm.Native.Mingw<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun mingwAll() {
            mingwX64()
            if (!holder.kotlinPluginVersion.isAtLeast(1, 9, 20)) {
                mingwX86()
            }
        }

        public fun mingwX64() { mingwX64 {} }

        public fun mingwX64(action: Action<X64>) {
            mingwX64("mingwX64", action)
        }

        public fun mingwX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun mingwX86() { mingwX86 {} }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun mingwX86(action: Action<X86>) {
            mingwX86("mingwX86", action)
        }

        @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
        public fun mingwX86(targetName: String, action: Action<X86>) {
            val container = holder.find(targetName) ?: X86(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String
    ): TargetMingwContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
    public class X86 internal constructor(
        targetName: String
    ): TargetMingwContainer<KotlinNativeTarget>(targetName)


    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = when (this@TargetMingwContainer) {
                is X64 -> {
                    mingwX64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is X86 -> {
                    mingwX86(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
            }

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${MINGW}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${MINGW}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 51

    internal companion object {
        internal const val MINGW = "mingw"
    }
}
