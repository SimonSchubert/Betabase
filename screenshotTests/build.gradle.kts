import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.inspiredandroid.betabase.screenshots"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets["main"].assets.srcDirs(
        "${project(":composeApp").projectDir}/build/generated/compose/resourceGenerator/preparedResources/commonMain",
    )
}

val preparePaparazzi by tasks.registering {
    dependsOn(":composeApp:prepareComposeResourcesTaskForCommonMain")
}

tasks.matching { it.name.startsWith("testDebug") }.configureEach {
    dependsOn(preparePaparazzi)
}

tasks.withType<Test>().configureEach {
    reports.html.required.set(false)
}

val snapshotsDir = layout.projectDirectory.dir("src/test/snapshots/images")
val mediaDir = layout.projectDirectory.dir("../media")
val fastlaneEnUsImagesDir = layout.projectDirectory.dir("../fastlane/metadata/android/en-US/images")

tasks.register("updateScreenshots") {
    dependsOn("recordPaparazziDebug")

    val snapshotsDirFile = snapshotsDir.asFile
    val mediaDirFile = mediaDir.asFile
    val phoneScreenshotsDirFile = File(fastlaneEnUsImagesDir.asFile, "phoneScreenshots")
    val storeImagesDirFile = fastlaneEnUsImagesDir.asFile

    doLast {
        val mediaMapping = mapOf(
            "ready" to "screen_01_ready.png",
            "loading" to "screen_02_loading.png",
            "error" to "screen_03_error.png",
            "filteredEmpty" to "screen_04_filtered_empty.png",
            "youthFiltered" to "screen_05_youth.png",
        )
        mediaDirFile.mkdirs()
        mediaMapping.forEach { (testName, mediaName) ->
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

        val phoneMapping = mapOf(
            "ready" to "01_ready.png",
            "youthFiltered" to "02_youth.png",
            "filteredEmpty" to "03_filters.png",
            "error" to "04_offline.png",
        )
        phoneScreenshotsDirFile.mkdirs()
        phoneMapping.forEach { (testName, fastlaneName) ->
            val snapshot = snapshotsDirFile.listFiles()?.find {
                it.name.endsWith("_ScreenshotTest_$testName.png")
            }
            if (snapshot != null) {
                snapshot.copyTo(File(phoneScreenshotsDirFile, fastlaneName), overwrite = true)
                println("Copied ${snapshot.name} -> fastlane/.../phoneScreenshots/$fastlaneName")
            }
        }

        // Play Store requires exact pixel sizes — Paparazzi can be off-by-a-few in
        // landscape, so we resize to spec on copy.
        val storeAssets = listOf(
            Triple("StoreIconTest_render", "icon.png", 512 to 512),
            Triple("StoreFeatureGraphicTest_render", "featureGraphic.png", 1024 to 500),
        )
        storeImagesDirFile.mkdirs()
        storeAssets.forEach { (testKey, fastlaneName, target) ->
            val snapshot = snapshotsDirFile.listFiles()?.find { it.name.contains("_$testKey.png") }
            if (snapshot == null) {
                println("Warning: no store asset snapshot found for $testKey")
                return@forEach
            }
            val out = File(storeImagesDirFile, fastlaneName)
            val src = ImageIO.read(snapshot)
            val dst = BufferedImage(target.first, target.second, BufferedImage.TYPE_INT_ARGB)
            val g = dst.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(src, 0, 0, target.first, target.second, null)
            g.dispose()
            out.parentFile?.mkdirs()
            ImageIO.write(dst, "png", out)
            println("Wrote ${target.first}x${target.second} ${out.name} from ${snapshot.name}")
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    testImplementation(libs.junit)
    testImplementation(libs.compose.runtime)
    testImplementation(libs.compose.material3)
    testImplementation(libs.compose.foundation)
    testImplementation(libs.compose.ui)
    testImplementation(libs.compose.components.resources)
    testImplementation(libs.kotlinx.datetime)
}
