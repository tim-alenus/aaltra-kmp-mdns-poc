## Kotlin Multiplatform mDNS POC

### Overview

This project demonstrates how to use Apple's **NWBrowser API** (a Swift-only framework) for mDNS service discovery within a Kotlin Multiplatform Mobile (KMM) application. The main challenge addressed here is bridging Swift-exclusive APIs to Kotlin code, enabling a iOS app written in KMP to perform network service discovery using the modern Network framework.

The KMP library [dns-sd-kt](https://github.com/Appstractive/dns-sd-kt) formed a start for this POC.
But unlike dns-sdk-kit, this POC uses NWBrowser instead of the older NetService API.
A continuation of this POC would be to integrate the learnings from this POC
fully into dns-sd-kt and create a pull request for that library. 

### Goal

The primary goal of this POC was to prove that Swift-only APIs like `NWBrowser` can be successfully integrated into Kotlin Multiplatform projects through C interop, allowing developers to leverage platform-specific capabilities while maintaining a shared Kotlin codebase.

### Architecture

The project uses a multi-layered architecture to bridge Swift to Kotlin:

1. **Swift Bridge Layer** (`NWBrowserBridge.swift`)
   - Wraps Apple's `NWBrowser` and `NWConnection` APIs
   - Provides an `@objc` interface that can be exposed to Kotlin
   - Handles mDNS service browsing and resolution
   - Manages connection lifecycle and error handling

2. **C Interop Definition** (`nwbrowser.def`)
   - Defines the interface between Swift and Kotlin
   - Enables Kotlin to call Swift code through C interop

3. **Kotlin Expect/Actual Pattern**
   - `Discovery.kt` (common): Defines the shared interface with `discoverServices()` expect function
   - `Discovery.ios.kt` (iOS): Implements the actual platform-specific logic using `NWBrowserBridge`
   - Returns a Kotlin `Flow<DiscoveryEvent>` for reactive service discovery

4. **Compose Multiplatform UI**
   - Shared UI code that works across Android and iOS
   - Displays discovered services in real-time
   - Uses Material 3 components for a modern look

### Key Features

- **mDNS Service Discovery**: Browse for services on the local network (e.g., `_http._tcp`)
- **Cross-platform**: Shared business logic and UI with platform-specific implementations
- **Build Integration**: Custom Gradle task to compile Swift code and link it with Kotlin

### Technical Highlights

- **Swift Compilation**: The `build.gradle.kts` includes a custom `compileSwift` task that compiles the Swift bridge into a static library (`.a` file)
- **C Interop**: Uses Kotlin's `cinterops` configuration to generate Kotlin bindings for the Objective-C interface
- **Callback Flow**: Converts callback-based Swift APIs into Kotlin's coroutine-based Flow API
- **Error Handling**: Properly handles network permission errors and DNS failures
- **Lifecycle Management**: Manages browser and connection lifecycle with proper cleanup

### Project Structure

```
shared/
  ├── src/
  │   ├── commonMain/kotlin/        # Shared Kotlin code
  │   │   └── Discovery.kt          # Common interface
  │   ├── iosMain/kotlin/           # iOS-specific Kotlin
  │   │   └── Discovery.ios.kt      # iOS implementation
  │   └── nativeInterop/
  │       ├── swift/
  │       │   └── NWBrowserBridge.swift  # Swift bridge
  │       └── cinterop/
  │           └── nwbrowser.def     # C interop definition
  └── build.gradle.kts              # Build configuration with Swift compilation
composeApp/                         # Compose Multiplatform UI
```

### Usage

The API provides a simple Flow-based interface:

```kotlin
discoverServices("_http._tcp").collect { event ->
    when (event) {
        is DiscoveryEvent.Discovered -> {
            // Service found, call resolve() to get details
            event.resolve()
        }
        is DiscoveryEvent.Resolved -> {
            // Service details received (IP, port, etc.)
        }
        is DiscoveryEvent.Removed -> {
            // Service left the network
        }
    }
}
```

### Requirements

- Kotlin Multiplatform 2.x
- iOS 15.0+
- Xcode with Swift compiler
- Compose Multiplatform

### Learnings

This POC demonstrates that:
- Swift-only APIs can be successfully integrated into KMM projects
- C interop provides a viable bridge between Swift and Kotlin
- Complex async Swift APIs can be wrapped into clean Kotlin Flow APIs
- Custom Gradle tasks can automate Swift compilation and linking
- The expect/actual pattern works well for platform-specific network APIs

