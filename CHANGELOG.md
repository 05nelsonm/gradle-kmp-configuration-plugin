# CHANGELOG

## Version 0.1.3 (2023-08-06)
 - Updates dependencies:
     - AGP -> `8.1.0`
 - `common` block now applies `pluginIds` before configuring 
   `kotlin.sourceSets` [[#24]][pr-24]

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
 - Adds support for composite builds [[#19]][pr-19]

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

[pr-19]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/19
[pr-24]: https://github.com/05nelsonm/gradle-kmp-configuration-plugin/pull/24
