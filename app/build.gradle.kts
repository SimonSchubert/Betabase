plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.inspiredandroid.betabase"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.inspiredandroid.betabase"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.android.versionCode.get().toInt()
        versionName = libs.versions.appVersion.get()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
    sourceSets["test"].kotlin.srcDir("src/test/kotlin")

    signingConfigs {
        create("release") {
            val envKeystore = System.getenv("KEYSTORE_FILE")
            val keystoreFile = if (envKeystore != null) {
                file(envKeystore)
            } else {
                rootProject.layout.projectDirectory.file("keystore.jks").asFile
            }
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val hasReleaseKeystore = System.getenv("KEYSTORE_FILE") != null ||
                rootProject.layout.projectDirectory.file("keystore.jks").asFile.exists()
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.foundation)
    testImplementation(libs.compose.foundation.layout)
    testImplementation(libs.compose.runtime)
    testImplementation(libs.compose.ui)
}

val snapshotsDir = layout.projectDirectory.dir("src/test/snapshots/images")
val mediaDir = layout.projectDirectory.dir("../media")

tasks.register("updateScreenshots") {
    dependsOn("recordPaparazziDebug")
    val snapshotsDirFile = snapshotsDir.asFile
    val mediaDirFile = mediaDir.asFile
    doLast {
        val mapping = mapOf(
            "ready" to "screen_01_ready.png",
            "loading" to "screen_02_loading.png",
            "error" to "screen_03_error.png",
            "filteredEmpty" to "screen_04_filtered_empty.png",
            "youthFiltered" to "screen_05_youth.png",
        )
        mediaDirFile.mkdirs()
        mapping.forEach { (testName, mediaName) ->
            val snapshot = snapshotsDirFile.listFiles()?.find {
                it.name.endsWith("_ScreenshotTest_$testName.png")
            }
            if (snapshot != null) {
                snapshot.copyTo(mediaDirFile.resolve(mediaName), overwrite = true)
                println("Copied ${snapshot.name} -> media/$mediaName")
            } else {
                println("Warning: no snapshot found for $testName")
            }
        }
    }
}
