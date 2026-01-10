plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "eu.aaltra.kmp.mdns.shared"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val swiftCompile = tasks.register<Exec>("compileSwift") {
        val sdkName = "iphoneos" // or "iphonesimulator" for simulator
        val targetArch = "arm64-apple-ios15.0"

        // 2. Resolve paths to Strings immediately
        val buildDirPath = project.layout.buildDirectory.get().asFile.absolutePath
        val outputFile = "$buildDirPath/swift/libNWBrowserBridge.a"
        val swiftFile = project.file("src/nativeInterop/swift/NWBrowserBridge.swift").absolutePath

        // Get the SDK path dynamically
        val sdkPath = providers.exec {
            commandLine("xcrun", "--sdk", sdkName, "--show-sdk-path")
        }.standardOutput.asText.get().trim()

        doFirst {
            file("${buildDirPath}/swift").mkdirs()
        }

        commandLine(
            "xcrun", "-sdk", sdkName, "swiftc",
            "-emit-library",
            "-static",
            "-target", targetArch,
            "-sdk", sdkPath,
            swiftFile,
            "-o", outputFile,
            "-module-name", "NWBrowserBridge",
            "-Xfrontend", "-serialize-debugging-options"
        )
        outputs.file(outputFile)
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    iosX64 {
        compilations.getByName("main") {
            cinterops {
                val nwbrowser by creating {
                    defFile(project.file("src/nativeInterop/cinterop/nwbrowser.def"))
                    packageName("eu.aaltra.kmp.mdns.nwbrowser")
                    includeDirs(project.file("src/nativeInterop/swift"))
                }
            }
        }
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        compilations.getByName("main") {
            cinterops {
                val nwbrowser by creating {
                    defFile(project.file("src/nativeInterop/cinterop/nwbrowser.def"))
                    packageName("eu.aaltra.kmp.mdns.nwbrowser")
                    includeDirs(project.file("src/nativeInterop/swift"))
                }
            }
            tasks.named("cinteropNwbrowserIosArm64") { dependsOn(swiftCompile) }
        }
        binaries.framework {
            baseName = xcfName
            linkerOpts("-L${layout.buildDirectory}/swift", "-lNWBrowserBridge")
        }
    }

    iosSimulatorArm64 {
        compilations.getByName("main") {
            cinterops {
                val nwbrowser by creating {
                    defFile(project.file("src/nativeInterop/cinterop/nwbrowser.def"))
                    packageName("eu.aaltra.kmp.mdns.nwbrowser")
                    includeDirs(project.file("src/nativeInterop/swift"))
                }
            }
        }
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {

                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}