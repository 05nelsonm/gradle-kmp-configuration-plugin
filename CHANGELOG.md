# CHANGELOG

## Version 0.4.0 (2025-01-24)
 - Updates dependencies [[#61]][61], [[#64]][64]:
     - AGP -> `8.7.3`
     - KGP -> `2.1.0`
     - Gradle Wrapper -> `8.12`
 - Removes `EperimentalKmpConfigurationApi` from `TargetJvmContainer.java9ModuleInfoName` [[#62]][62]

## Version 0.3.2 (2024-08-29)
 - Fixes `moduleName` for Jvm/Android to replace character `:` with `_`
   in order to prevent Windows from blowing up.

## Version 0.3.1 (2024-08-29)
 - Fixes `pluginIds` functionality to collect all configured id's and
   apply them before configuring anything.
 - Adds `OptionContainer.useUniqueModuleNames` to autoconfigure metadata
   and Jvm/Android compilerOption `moduleName` with a truly unique value.

## Version 0.3.0 (2024-06-15)
 - Update dependencies [[#53]][53]:
     - KGP -> `2.0.0`

## Version 0.2.2 (2024-06-15)
 - Update dependencies [[#49]][49]:
     - AGP -> `8.5.0`
     - KGP -> `1.9.24`
     - Gradle Wrapper -> `8.8`
 - Replace `TargetJvmContainer.java9MultiReleaseModuleInfo` function with
   variable `java9ModuleInfoName` & deprecate [[#50]][50]

## Version 0.2.1 (2024-03-10)
 - Update dependencies:
     - KGP -> `1.9.23`
 - Adds experimental support to `jvm` for configuring
   multi-release Jars to include `module-info.java` [[#45]][45]

## Version 0.2.0 (2024-02-25)
 - Update dependencies:
     - AGP -> `8.2.2`
     - KGP -> `1.9.22`
     - Gradle Wrapper -> `8.6`
 - `androidNative` intermediary source set now inherits from `unix` instead of 
   `native` [[#43]][43]
     - **NOTE:** This may be a breaking change for consumers.

## Version 0.1.7 (2023-12-26)
 - Fixes the option `nonSimulatorSourceSets` which had `iOS`, `tvOS`, `watchOS`
   `X64` targets inheriting from it (`X64` targets are simulators) [[#40]][40]
 - Deprecates `nonSimulatorSourceSets` in favor of `useNonSimulatorSourceSets`
 - Adds `useSimulatorSourceSets` option which does the same thing as 
   `useNonSimulatorSourceSets`, but puts simulators on `{ios/tvos/watchos}Simulator{Main/Test}`

## Version 0.1.6 (2023-12-13)
 - Update dependencies:
     - AGP -> `8.2.0`
     - KGP -> `1.9.21`
     - Gradle Wrapper -> `8.5`
 - Adds a new `Options` container with an initial purpose of enabling an 
   option for configuring additional source sets for `iOS`, `tvOS`, and 
   `watchOS` non-simulator targets to inherit from. [[#35]][35]

## Version 0.1.5 (2023-11-20)
 - Update dependencies:
     - AGP -> `8.1.4`
     - KGP -> `1.9.20`
     - Gradle Wrapper -> `8.4`
 - Adds `wasmJs` and `wasmWasi` containers
     - **NOTE:** if you are invoking the `wasm` container, 
       you **must** use Kotlin `1.9.20` or greater. This is because
       Kotlin's `KotlinWasmJsTargetDsl` interface was changed in `1.9.20`. 
     - To add support, there was an API breaking change
       that was needed. The `TargetWasmContainer` was changed
       to a `sealed class`. There is now:
         - `TargetWasmContainer.WasmJs`
         - `TargetWasmContainer.WasmWasi`
         - `TargetWasmContainer.Wasm` (deprecated)
 - Add deprecations with `ERROR` for the following targets
     - `iosArm32`
     - `watchosX86`
     - `linuxArm32Hfp`
     - `linuxMips32`
     - `linuxMipsel32`
     - `mingwX86`
     - `wasm32`

## Version 0.1.4 (2023-09-29)
 - Update dependencies:
     - KGP -> `1.9.10`
     - Gradle Wrapper -> `8.3`

## Version 0.1.3 (2023-08-06)
 - Updates dependencies:
     - AGP -> `8.1.0`
 - `common` block now applies `pluginIds` before configuring 
   `kotlin.sourceSets` [[#24]][24]

## Version 0.1.2 (2023-08-01)
 - Updates dependencies:
     - KGP -> `1.9.0`
     - Gradle Wrapper -> `8.2.1`

## Version 0.1.1 (2023-06-02)
 - Fixes stuck publication for `0.1.0`

## Version 0.1.0 (2023-06-01)
 - Removes default `wasm()` setup
 - Updates dependencies:
     - AGP -> `8.0.2`
     - KGP -> `1.8.21`
         - Adds deprecation notices in line with KGP release
     - Gradle Wrapper -> `8.1.1`

## Version 0.1.0-beta02 (2023-03-06)
 - Adds support for composite builds [[#19]][19]

## Version 0.1.0-beta01 (2023-03-02)
 - Fixes internal API compatibility issues with Kotlin `1.8.20-Beta`
 - Updates dependencies:
     - AGP -> `7.4.1`
     - KGP -> `1.8.10`
     - Gradle Wrapper -> `8.0.1`
 - Increases minimum supported JavaVersion to `11`

## Version 0.1.0-alpha02 (2023-02-16)
 - Add check for Kotlin Gradle Plugin version for API compatibility

## Version 0.1.0-alpha01 (2023-02-09)
 - Initial Release

[19]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/19
[24]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/24
[35]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/35
[40]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/40
[43]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/43
[45]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/45
[49]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/49
[50]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/50
[53]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/53
[61]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/61
[62]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/62
[64]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/64
