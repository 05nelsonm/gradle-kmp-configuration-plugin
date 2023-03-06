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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

public sealed class TargetAndroidNativeContainer<T: KotlinNativeTarget> private constructor(
    targetName: String
): KmpTarget.NonJvm.Native.Android<T>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun androidNativeAll() {
            androidNativeArm32()
            androidNativeArm64()
            androidNativeX64()
            androidNativeX86()
        }

        public fun androidNativeArm32() { androidNativeArm32 {} }

        public fun androidNativeArm32(action: Action<Arm32>) {
            androidNativeArm32("androidNativeArm32", action)
        }

        public fun androidNativeArm32(targetName: String, action: Action<Arm32>) {
            val container = holder.find(targetName) ?: Arm32(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun androidNativeArm64() { androidNativeArm64 {} }

        public fun androidNativeArm64(action: Action<Arm64>) {
            androidNativeArm64("androidNativeArm64", action)
        }

        public fun androidNativeArm64(targetName: String, action: Action<Arm64>) {
            val container = holder.find(targetName) ?: Arm64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun androidNativeX64() { androidNativeX64 {} }

        public fun androidNativeX64(action: Action<X64>) {
            androidNativeX64("androidNativeX64", action)
        }

        public fun androidNativeX64(targetName: String, action: Action<X64>) {
            val container = holder.find(targetName) ?: X64(targetName)
            action.execute(container)
            holder.add(container)
        }

        public fun androidNativeX86() { androidNativeX86 {} }

        public fun androidNativeX86(action: Action<X86>) {
            androidNativeX86("androidNativeX86", action)
        }

        public fun androidNativeX86(targetName: String, action: Action<X86>) {
            val container = holder.find(targetName) ?: X86(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    @KmpConfigurationDsl
    public class Arm32 internal constructor(
        targetName: String
    ): TargetAndroidNativeContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class Arm64 internal constructor(
        targetName: String
    ): TargetAndroidNativeContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X64 internal constructor(
        targetName: String
    ): TargetAndroidNativeContainer<KotlinNativeTarget>(targetName)

    @KmpConfigurationDsl
    public class X86 internal constructor(
        targetName: String
    ): TargetAndroidNativeContainer<KotlinNativeTarget>(targetName)

    @JvmSynthetic
    internal final override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            @Suppress("RedundantSamConstructor")
            val target = when (this@TargetAndroidNativeContainer) {
                is Arm32 -> {
                    androidNativeArm32(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is Arm64 -> {
                    androidNativeArm64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is X64 -> {
                    androidNativeX64(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
                is X86 -> {
                    androidNativeX86(targetName, Action { t ->
                        lazyTarget.forEach { action -> action.execute(t) }
                    })
                }
            }

            applyPlugins(target.project)

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${ANDROID_NATIVE}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${ANDROID_NATIVE}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }
        }
    }

    @JvmSynthetic
    internal final override val sortOrder: Byte = 21

    internal companion object {
        internal const val ANDROID_NATIVE = "androidNative"
    }
}
