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

import io.matthewnelson.kmp.configuration.extension.container.Container
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

public sealed class KmpTarget<T: KotlinTarget> private constructor(
    internal val targetName: String
): Container.ConfigurableTarget() {

    protected val lazyTarget: MutableList<Action<T>> = mutableListOf()
    public fun target(action: Action<T>) { lazyTarget.add(action) }

    public sealed class Jvm<T: KotlinTarget>(targetName: String): KmpTarget<T>(targetName) {

        internal companion object { internal const val JVM_ANDROID = "jvmAndroid" }

        public var kotlinJvmTarget: JavaVersion? = null
        public var compileSourceCompatibility: JavaVersion? = null
        public var compileTargetCompatibility: JavaVersion? = null
    }

    public sealed class NonJvm<T: KotlinTarget>(targetName: String): KmpTarget<T>(targetName) {

        internal companion object { internal const val NON_JVM = "nonJvm" }

        public sealed class Native<T: KotlinNativeTarget>(targetName: String): NonJvm<T>(targetName) {

            internal companion object { internal const val NATIVE = "native" }

            public sealed class Android<T: KotlinNativeTarget>(targetName: String): Native<T>(targetName)

            public sealed class Unix<T: KotlinNativeTarget>(targetName: String): Native<T>(targetName) {

                internal companion object { internal const val UNIX = "unix" }

                public sealed class Darwin<T: KotlinNativeTarget>(targetName: String): Unix<T>(targetName) {

                    internal companion object { internal const val DARWIN = "darwin" }

                    public sealed class Ios<T: KotlinNativeTarget>(targetName: String): Darwin<T>(targetName)
                    public sealed class Macos<T: KotlinNativeTarget>(targetName: String): Darwin<T>(targetName)
                    public sealed class Tvos<T: KotlinNativeTarget>(targetName: String): Darwin<T>(targetName)
                    public sealed class Watchos<T: KotlinNativeTarget>(targetName: String): Darwin<T>(targetName)
                }

                public sealed class Linux<T: KotlinNativeTarget>(targetName: String): Unix<T>(targetName)
            }

            public sealed class Mingw<T: KotlinNativeTarget>(targetName: String): Native<T>(targetName)

            @Deprecated(message = "Target is deprecated and will be removed soon: see https://kotl.in/native-targets-tiers")
            public sealed class Wasm<T: KotlinNativeTarget>(targetName: String): Native<T>(targetName)
        }
    }
}
