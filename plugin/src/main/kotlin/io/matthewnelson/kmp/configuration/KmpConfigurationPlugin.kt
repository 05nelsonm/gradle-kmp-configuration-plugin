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
package io.matthewnelson.kmp.configuration

import io.matthewnelson.kmp.configuration.extension.KmpConfigurationExtension
import io.matthewnelson.kmp.configuration.extension.container.CommonContainer
import io.matthewnelson.kmp.configuration.extension.container.Container
import io.matthewnelson.kmp.configuration.extension.container.KotlinExtensionActionContainer
import io.matthewnelson.kmp.configuration.extension.container.target.*
import io.matthewnelson.kmp.configuration.extension.container.target.KmpTargetProperty.Companion.findKmpTargetsProperties
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

public open class KmpConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val kotlinPluginVersion = target.kotlinPluginVersionOrNull()
            ?: throw GradleException("Failed to determine Kotlin Plugin Version")

        target.extensions.create(
            KmpConfigurationExtension.NAME,
            KmpConfigurationExtension::class.java,
            kotlinPluginVersion,
            KmpTargetProperty.isKmpTargetsAllSet,
            target.findKmpTargetsProperties(),
            Action<Set<Container>> { containers -> target.configure(containers) }
        )
    }

    private fun Project.kotlinPluginVersionOrNull(): KotlinVersion? {
        return getKotlinPluginVersion().let { versionString ->
            val baseVersion = versionString.split("-", limit = 2)[0]

            val baseVersionSplit = baseVersion.split(".")
            if (!(baseVersionSplit.size == 2 || baseVersionSplit.size == 3)) return null

            KotlinVersion(
                major = baseVersionSplit[0].toIntOrNull() ?: return null,
                minor = baseVersionSplit[1].toIntOrNull() ?: return null,
                patch = baseVersionSplit.getOrNull(2)?.let { it.toIntOrNull() ?: return null } ?: 0,
            )
        }
    }

    private fun Project.configure(containers: Set<Container>) {
        val targets = containers.filterIsInstance<KmpTarget<*>>()
        if (targets.isEmpty()) return

        plugins.apply("org.jetbrains.kotlin.multiplatform")

        extensions.configure(KotlinMultiplatformExtension::class.java) { kmp ->
            kmp.sourceSets.setupIntermediateSourceSets(targets)

            for (target in targets) {
                if (target is TargetAndroidContainer<*>) {
                    if (target is TargetAndroidContainer.App) {
                        plugins.apply("com.android.application")
                    } else {
                        plugins.apply("com.android.library")
                    }
                }

                target.setup(kmp)
            }

            containers.filterIsInstance<CommonContainer>().forEach { commonContainer ->
                commonContainer.setup(kmp)
            }

            containers.filterIsInstance<KotlinExtensionActionContainer>().forEach { kotlinContainer ->
                kotlinContainer.setup(kmp)
            }
        }
    }

    private fun NamedDomainObjectContainer<KotlinSourceSet>.setupIntermediateSourceSets(targets: List<KmpTarget<*>>) {
        val commonMain = getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
        val commonTest = getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)

        val jvmTargets = targets.filterIsInstance<KmpTarget.Jvm<*>>()
        if (jvmTargets.isNotEmpty()) {
            maybeCreate("${KmpTarget.Jvm.JVM_ANDROID}Main").apply {
                dependsOn(commonMain)
            }
            maybeCreate("${KmpTarget.Jvm.JVM_ANDROID}Test").apply {
                dependsOn(commonTest)
            }
        }

        val nonJvmTargets = targets.filterIsInstance<KmpTarget.NonJvm<*>>()
        if (nonJvmTargets.isNotEmpty()) {
            val nonJvmMain = maybeCreate("${KmpTarget.NonJvm.NON_JVM}Main").apply {
                dependsOn(commonMain)
            }
            val nonJvmTest = maybeCreate("${KmpTarget.NonJvm.NON_JVM}Test").apply {
                dependsOn(commonTest)
            }

            val nativeTargets = nonJvmTargets.filterIsInstance<KmpTarget.NonJvm.Native<*>>()
            if (nativeTargets.isNotEmpty()) {
                val nativeMain = maybeCreate("${KmpTarget.NonJvm.Native.NATIVE}Main").apply {
                    dependsOn(nonJvmMain)
                }
                val nativeTest = maybeCreate("${KmpTarget.NonJvm.Native.NATIVE}Test").apply {
                    dependsOn(nonJvmTest)
                }

                val androidNativeTargets = nativeTargets.filterIsInstance<KmpTarget.NonJvm.Native.Android<*>>()
                if (androidNativeTargets.isNotEmpty()) {
                    maybeCreate("${TargetAndroidNativeContainer.ANDROID_NATIVE}Main").dependsOn(nativeMain)
                    maybeCreate("${TargetAndroidNativeContainer.ANDROID_NATIVE}Test").dependsOn(nativeTest)
                }

                val unixTargets = nativeTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix<*>>()
                if (unixTargets.isNotEmpty()) {
                    val unixMain = maybeCreate("${KmpTarget.NonJvm.Native.Unix.UNIX}Main").apply {
                        dependsOn(nativeMain)
                    }
                    val unixTest = maybeCreate("${KmpTarget.NonJvm.Native.Unix.UNIX}Test").apply {
                        dependsOn(nativeTest)
                    }

                    val darwinTargets = unixTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Darwin<*>>()
                    if (darwinTargets.isNotEmpty()) {
                        val darwinMain = maybeCreate("${KmpTarget.NonJvm.Native.Unix.Darwin.DARWIN}Main").apply {
                            dependsOn(unixMain)
                        }
                        val darwinTest = maybeCreate("${KmpTarget.NonJvm.Native.Unix.Darwin.DARWIN}Test").apply {
                            dependsOn(unixTest)
                        }

                        val iosTargets = darwinTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Darwin.Ios<*>>()
                        if (iosTargets.isNotEmpty()) {
                            maybeCreate("${TargetIosContainer.IOS}Main").dependsOn(darwinMain)
                            maybeCreate("${TargetIosContainer.IOS}Test").dependsOn(darwinTest)
                        }

                        val macosTargets = darwinTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Darwin.Macos<*>>()
                        if (macosTargets.isNotEmpty()) {
                            maybeCreate("${TargetMacosContainer.MACOS}Main").dependsOn(darwinMain)
                            maybeCreate("${TargetMacosContainer.MACOS}Test").dependsOn(darwinTest)
                        }

                        val tvosTargets = darwinTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Darwin.Tvos<*>>()
                        if (tvosTargets.isNotEmpty()) {
                            maybeCreate("${TargetTvosContainer.TVOS}Main").dependsOn(darwinMain)
                            maybeCreate("${TargetTvosContainer.TVOS}Test").dependsOn(darwinTest)
                        }

                        val watchosTargets = darwinTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Darwin.Watchos<*>>()
                        if (watchosTargets.isNotEmpty()) {
                            maybeCreate("${TargetWatchosContainer.WATCHOS}Main").dependsOn(darwinMain)
                            maybeCreate("${TargetWatchosContainer.WATCHOS}Test").dependsOn(darwinTest)
                        }
                    }

                    val linuxTargets = unixTargets.filterIsInstance<KmpTarget.NonJvm.Native.Unix.Linux<*>>()
                    if (linuxTargets.isNotEmpty()) {
                        maybeCreate("${TargetLinuxContainer.LINUX}Main").dependsOn(unixMain)
                        maybeCreate("${TargetLinuxContainer.LINUX}Test").dependsOn(unixTest)
                    }
                }

                val mingwTargets = nativeTargets.filterIsInstance<KmpTarget.NonJvm.Native.Mingw<*>>()
                if (mingwTargets.isNotEmpty()) {
                    maybeCreate("${TargetMingwContainer.MINGW}Main").dependsOn(nativeMain)
                    maybeCreate("${TargetMingwContainer.MINGW}Test").dependsOn(nativeTest)
                }

                @Suppress("DEPRECATION")
                val wasmNativeTargets = nativeTargets.filterIsInstance<KmpTarget.NonJvm.Native.Wasm<*>>()
                @Suppress("DEPRECATION_ERROR")
                if (wasmNativeTargets.isNotEmpty()) {
                    maybeCreate("${TargetWasmNativeContainer.WASM_NATIVE}Main").dependsOn(nativeMain)
                    maybeCreate("${TargetWasmNativeContainer.WASM_NATIVE}Test").dependsOn(nativeTest)
                }
            }
        }
    }
}
