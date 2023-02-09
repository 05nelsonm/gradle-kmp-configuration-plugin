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

import org.gradle.api.GradleException
import org.gradle.api.Project

public enum class KmpTargetProperty {
    ANDROID,
    ANDROID_ARM32,
    ANDROID_ARM64,
    ANDROID_X64,
    ANDROID_X86,
    JVM,
    JS,
    LINUX_ARM32HFP,
    LINUX_ARM64,
    LINUX_MIPS32,
    LINUX_MIPSEL32,
    LINUX_X64,
    MINGW_X64,
    MINGW_X86,
    IOS_ARM32,
    IOS_ARM64,
    IOS_SIMULATOR_ARM64,
    IOS_X64,
    MACOS_ARM64,
    MACOS_X64,
    TVOS_ARM64,
    TVOS_SIMULATOR_ARM64,
    TVOS_X64,
    WATCHOS_ARM32,
    WATCHOS_ARM64,
    WATCHOS_DEVICE_ARM64,
    WATCHOS_SIMULATOR_ARM64,
    WATCHOS_X64,
    WATCHOS_X86,
    WASM,
    WASM_32;

    internal companion object {
        @get:JvmSynthetic
        internal val isKmpTargetsAllSet: Boolean get() = System.getProperty("KMP_TARGETS_ALL") != null

        @JvmSynthetic
        @Throws(GradleException::class)
        internal fun Project.findKmpTargetsProperties(): Set<KmpTargetProperty>? {
            return findProperty("KMP_TARGETS")
                ?.toString()
                ?.split(',')
                ?.map { target ->
                    try {
                        KmpTargetProperty.valueOf(target.trim())
                    } catch (e: IllegalArgumentException) {
                        throw GradleException("KMP_TARGETS property of '$target' not recognized", e)
                    }
                }
                ?.toSet()
        }

        @JvmSynthetic
        internal fun KmpTarget<*>.property(): KmpTargetProperty {
            return when (this) {
                is TargetAndroidContainer<*> -> ANDROID
                is TargetAndroidNativeContainer.Arm32 -> ANDROID_ARM32
                is TargetAndroidNativeContainer.Arm64 -> ANDROID_ARM64
                is TargetAndroidNativeContainer.X64 -> ANDROID_X64
                is TargetAndroidNativeContainer.X86 -> ANDROID_X86
                is TargetJvmContainer -> JVM
                is TargetJsContainer -> JS
                is TargetLinuxContainer.Arm32Hfp -> LINUX_ARM32HFP
                is TargetLinuxContainer.Arm64 -> LINUX_ARM64
                is TargetLinuxContainer.Mips32 -> LINUX_MIPS32
                is TargetLinuxContainer.Mipsel32 -> LINUX_MIPSEL32
                is TargetLinuxContainer.X64 -> LINUX_X64
                is TargetMingwContainer.X64 -> MINGW_X64
                is TargetMingwContainer.X86 -> MINGW_X86
                is TargetIosContainer.Arm32 -> IOS_ARM32
                is TargetIosContainer.Arm64 -> IOS_ARM64
                is TargetIosContainer.SimulatorArm64 -> IOS_SIMULATOR_ARM64
                is TargetIosContainer.X64 -> IOS_X64
                is TargetMacosContainer.Arm64 -> MACOS_ARM64
                is TargetMacosContainer.X64 -> MACOS_X64
                is TargetTvosContainer.Arm64 -> TVOS_ARM64
                is TargetTvosContainer.SimulatorArm64 -> TVOS_SIMULATOR_ARM64
                is TargetTvosContainer.X64 -> TVOS_X64
                is TargetWatchosContainer.Arm32 -> WATCHOS_ARM32
                is TargetWatchosContainer.Arm64 -> WATCHOS_ARM64
                is TargetWatchosContainer.DeviceArm64 -> WATCHOS_DEVICE_ARM64
                is TargetWatchosContainer.SimulatorArm64 -> WATCHOS_SIMULATOR_ARM64
                is TargetWatchosContainer.X64 -> WATCHOS_X64
                is TargetWatchosContainer.X86 -> WATCHOS_X86
                is TargetWasmNativeContainer._32 -> WASM_32
                is TargetWasmContainer -> WASM
            }
        }
    }
}
