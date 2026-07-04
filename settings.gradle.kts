import java.io.File
import java.util.Properties

// --- Self-Healing Android SDK Auto-Detection ---
val localProps = File(rootDir, "local.properties")
var isSdkInvalid = false
val props = Properties()

if (localProps.exists()) {
    try {
        localProps.inputStream().use { props.load(it) }
        val sdkDirVal = props.getProperty("sdk.dir")
        if (sdkDirVal != null) {
            val sdkDir = File(sdkDirVal)
            if (!sdkDir.exists() || !sdkDir.isDirectory) {
                isSdkInvalid = true
            }
        } else {
            isSdkInvalid = true
        }
    } catch (e: Exception) {
        isSdkInvalid = true
    }
} else {
    isSdkInvalid = true
}

if (isSdkInvalid) {
    println("[SDK Resolver] Local SDK path is missing or invalid. Attempting auto-detection...")
    var detectedSdk: String? = null

    // 1. Check common system environment variables
    val envSdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (envSdk != null && File(envSdk).exists()) {
        detectedSdk = envSdk
    }

    // 2. Check default operating system installation folders
    if (detectedSdk == null) {
        val userHome = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()
        val pathsToCheck = if (osName.contains("win")) {
            listOf("$userHome\\AppData\\Local\\Android\\Sdk", "C:\\Android\\Sdk")
        } else if (osName.contains("mac")) {
            listOf("$userHome/Library/Android/sdk")
        } else {
            listOf("$userHome/Android/Sdk")
        }

        for (path in pathsToCheck) {
            if (File(path).exists()) {
                detectedSdk = path
                break
            }
        }
    }

    // 3. Apply the detected SDK and write/rewrite local.properties
    if (detectedSdk != null) {
        // Double escape backslashes for Windows path format in properties
        val sanitizedPath = File(detectedSdk).absolutePath.replace("\\", "\\\\")
        props.setProperty("sdk.dir", sanitizedPath)
        localProps.outputStream().use { props.store(it, "Auto-configured by Self-Healing Gradle Script") }
        println("[SDK Resolver] Automatically mapped Android SDK to: $detectedSdk")
    } else {
        System.err.println("[SDK Resolver] WARNING: Failed to auto-detect your Android SDK. Please set the ANDROID_HOME environment variable.")
    }
}
// ------------------------------------------------

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FileFlip"
include(":app")
 