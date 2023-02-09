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
plugins {
    id("shared")
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("io.matthewnelson.kmp.configuration") {
            id = "io.matthewnelson.kmp.configuration"
            implementationClass = "io.matthewnelson.kmp.configuration.KmpConfigurationPlugin"
            displayName = "Gradle Kotlin Multiplatform Configuration Plugin"
            description = "Gradle plugin which makes configuring Kotlin Multiplatform more enjoyable and extensible"
        }
    }
}

kotlin {
    explicitApi()
}

dependencies {
    compileOnly(libs.gradle.kotlin)
    compileOnly(libs.gradle.android)
}

signing {
    useGpgCmd()
}
