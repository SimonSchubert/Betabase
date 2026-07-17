import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spotless)
}

kotlin {
    applyDefaultHierarchyTemplate()
    android {
        namespace = "com.inspiredandroid.betabase"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Pin desktop bytecode to 21 so it matches the JDK used by compose.desktop
    // run/jpackage (see javaHome below). Kotlin 2.4 defaults higher (JVM 25 /
    // class file 69), which then fails at runtime on jdk-21 with
    // UnsupportedClassVersionError.
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val rootDirPath = rootProject.layout.projectDirectory.asFile.path
            val projectDirPath = layout.projectDirectory.asFile.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static(rootDirPath)
                        static(projectDirPath)
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        val nonWebMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(nonWebMain)
        iosMain.get().dependsOn(nonWebMain)
        desktopMain.dependsOn(nonWebMain)

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            val maplibreJni =
                libs.maplibre.native.bindings.jni
                    .get()
            runtimeOnly("${maplibreJni.module}:${maplibreJni.versionConstraint.requiredVersion}") {
                capabilities {
                    requireCapability("org.maplibre.compose:maplibre-native-bindings-jni-${detectMaplibreTarget()}")
                }
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        nonWebMain.dependencies {
            implementation(libs.maplibre.compose)
        }
    }
}

composeCompiler {
    val composeReports = providers.gradleProperty("composeCompilerReports").orNull == "true"
    if (composeReports) {
        reportsDestination = layout.buildDirectory.dir("compose/reports")
        metricsDestination = layout.buildDirectory.dir("compose/metrics")
    }
}

compose.desktop {
    application {
        mainClass = "com.inspiredandroid.betabase.MainKt"

        // jlink/jpackage need a vanilla JDK: the JetBrains Runtime that runs Gradle ships patched
        // modules whose hashes vanilla jlink rejects ("Hash of java.xml differs ... recorded in
        // java.base"). Point packaging at a real JDK 21 — overridable via -PjlinkJdk=<path> for CI.
        (findProperty("jlinkJdk") as String? ?: System.getenv("JLINK_JDK"))?.let { javaHome = it }
            ?: run {
                val fallback = "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
                if (file(fallback).exists()) javaHome = fallback
            }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Betabase"
            packageVersion = "1.0.0"
            // Ktor/Coil's OkHttp engine and TLS are resolved reflectively at runtime, so jlink's
            // static analysis (suggestRuntimeModules) doesn't detect them and strips them from the
            // bundled runtime — crashing the release build the first time a sponsor image loads
            // (NoClassDefFoundError: java/net/http/HttpClient$Version). Declare the dynamically
            // loaded modules explicitly: java.net.http for the HTTP client, jdk.crypto.ec for
            // HTTPS/TLS, java.naming for DNS, plus the four suggestRuntimeModules reported.
            modules(
                "java.net.http",
                "jdk.crypto.ec",
                "java.naming",
                "java.instrument",
                "java.management",
                "java.prefs",
                "jdk.unsupported",
            )
        }
    }
}

fun detectMaplibreTarget(): String {
    val hostOs =
        when (val os = System.getProperty("os.name").lowercase()) {
            "mac os x" -> "macos"
            else -> os.split(" ").first()
        }
    val hostArch =
        when (val arch = System.getProperty("os.arch").lowercase()) {
            "x86_64" -> "amd64"
            "arm64" -> "aarch64"
            else -> arch
        }
    val renderer = if (hostOs == "macos") "metal" else "opengl"
    return "$hostOs-$hostArch-$renderer"
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.5.0").editorConfigOverride(
            mapOf(
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_property-naming" to "disabled",
            ),
        )
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.5.0")
    }
}
