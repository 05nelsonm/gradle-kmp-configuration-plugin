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
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

@KmpConfigurationDsl
public class TargetJvmContainer internal constructor(
    targetName: String
): KmpTarget.Jvm<KotlinJvmTarget>(targetName) {

    public sealed interface Configure {
        public val holder: ContainerHolder

        public fun jvm() { jvm {} }

        public fun jvm(action: Action<TargetJvmContainer>) {
            jvm("jvm", action)
        }

        public fun jvm(targetName: String, action: Action<TargetJvmContainer>) {
            val container = holder.find(targetName) ?: TargetJvmContainer(targetName)
            action.execute(container)
            holder.add(container)
        }
    }

    /**
     * Configures things to utilize multi-release Jars in order
     * to support adding of Java9 `module-info.java`
     *
     * Adds the task "compileJavaModuleInfo" to the project
     *
     * Requires the following file be present:
     *
     * `src/<target-name>Main/java9/module-info.java`
     *
     * e.g.
     *
     *     java9ModuleInfoName = "io.matthewnelson.my.pkg.name"
     * */
    @JvmField
    public var java9ModuleInfoName: String? = null

    @JvmSynthetic
    internal override fun setup(kotlin: KotlinMultiplatformExtension) {
        with(kotlin) {
            val target = jvm(targetName, Action { t ->
                kotlinJvmTarget?.let { version ->
                    t.compilations.all {
                        it.kotlinOptions.jvmTarget = version.toString()
                    }
                }

                lazyTarget.forEach { action -> action.execute(t) }

                if (!t.withJavaEnabled) return@Action

                val sCompatibility = compileSourceCompatibility
                val tCompatibility = compileTargetCompatibility

                if (sCompatibility == null && tCompatibility == null) return@Action

                t.project.extensions.configure(JavaPluginExtension::class.java) { extension ->
                    if (sCompatibility != null) {
                        extension.sourceCompatibility = sCompatibility
                    }
                    if (tCompatibility != null) {
                        extension.targetCompatibility = tCompatibility
                    }
                }
            })

            with(sourceSets) {
                getByName("${targetName}Main") { ss ->
                    ss.dependsOn(getByName("${JVM_ANDROID}Main"))
                    lazySourceSetMain.forEach { action -> action.execute(ss) }
                }
                getByName("${targetName}Test") { ss ->
                    ss.dependsOn(getByName("${JVM_ANDROID}Test"))
                    lazySourceSetTest.forEach { action -> action.execute(ss) }
                }
            }

            configureJava9ModuleInfoMultiRelease(target)
        }
    }

    private fun configureJava9ModuleInfoMultiRelease(target: KotlinJvmTarget) {
        val moduleName = java9ModuleInfoName ?: return

        val java9Dir = target.project
            .projectDir
            .resolve("src")
            .resolve(targetName + "Main")
            .resolve("java9")

        if (!java9Dir.resolve("module-info.java").exists()) {
            throw GradleException("module-info.java not found in $java9Dir")
        }

        val javaToolchain = target.project.extensions.getByType(JavaToolchainService::class.java)
        val compileJavaModuleInfo = target.project.tasks.register("compileJavaModuleInfo", JavaCompile::class.java) { jCompile ->
            val compileKotlinTask = target.compilations.getByName("main").compileTaskProvider.get() as KotlinJvmCompile
            val targetDir = compileKotlinTask.destinationDirectory.dir("../java9")

            jCompile.javaCompiler.set(javaToolchain.compilerFor { config ->
                config.languageVersion.set(JavaLanguageVersion.of(11))
            })

            jCompile.dependsOn(compileKotlinTask)
            jCompile.source(java9Dir)

            if (compileKotlinTask.kotlinJavaToolchain.javaVersion.get().isJava9Compatible) {
                compileKotlinTask.source(java9Dir)
            }

            jCompile.outputs.dir(targetDir)
            jCompile.destinationDirectory.set(targetDir)
            jCompile.sourceCompatibility = JavaVersion.VERSION_1_9.toString()
            jCompile.targetCompatibility = JavaVersion.VERSION_1_9.toString()
            jCompile.options.release.set(9)
            jCompile.options.compilerArgs.add("-Xlint:-requires-transitive-automatic")
            jCompile.options.compilerArgs.addAll(listOf(
                "--patch-module",
                "$moduleName=${compileKotlinTask.destinationDirectory.get()}"
            ))
            jCompile.classpath = compileKotlinTask.libraries
            jCompile.modularity.inferModulePath.set(true)
        }

        target.project.tasks.withType(Jar::class.java) { jar ->
            if (jar.name != "${targetName}Jar") return@withType

            jar.manifest { manifest ->
                manifest.attributes["Multi-Release"] = true
            }
            jar.from(compileJavaModuleInfo.map { it.destinationDirectory }) { spec ->
                spec.into("META-INF/versions/9/")
            }
        }
    }

    @JvmSynthetic
    internal override val sortOrder: Byte = 2

    @KmpConfigurationDsl
    @Deprecated("Use variable setter java9ModuleName", ReplaceWith("this.java9ModuleInfoName = moduleName"))
    public fun java9MultiReleaseModuleInfo(moduleName: String?) {
        this.java9ModuleInfoName = moduleName
    }
}
