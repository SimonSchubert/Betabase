package com.inspiredandroid.betabase.screenshots

import android.graphics.BitmapFactory
import android.graphics.Color
import app.cash.paparazzi.Paparazzi
import coil3.ColorImage
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest

/**
 * Downloads live remote images once (cached under `build/fixture-cache/`), then
 * installs a Paparazzi Coil loader that serves them synchronously.
 *
 * Nothing under `src/` is committed — only the rendered store PNGs. Requires
 * network on first run / cold cache; re-runs reuse the cache until
 * `./gradlew clean` (or deleting `screenshotTests/build/fixture-cache`).
 */
@OptIn(DelicateCoilApi::class)
fun Paparazzi.installLiveImageLoader(urls: Collection<String>) {
    val unique = urls.map { it.trim() }.filter { it.startsWith("http") }.distinct()
    val cacheDir = fixtureCacheDir().also { it.mkdirs() }
    val fallback = ColorImage(Color.rgb(0x3D, 0x4A, 0x5C), width = 320, height = 180)

    // Match keys Coil may pass as String / Uri: full URL, path, last path segment.
    val imagesByKey = linkedMapOf<String, Image>()
    var ok = 0
    var fail = 0
    unique.forEach { url ->
        val image = downloadToImage(url, cacheDir)
        if (image != null) {
            ok++
            matchKeysFor(url).forEach { key -> imagesByKey.putIfAbsent(key, image) }
        } else {
            fail++
            System.err.println("Screenshot fixture download failed: $url")
        }
    }
    println("Screenshot image fixtures: $ok downloaded/cached, $fail failed (of ${unique.size})")

    val builder = FakeImageLoaderEngine.Builder()
    // Longer keys first so full URLs win over short path segments when both match.
    imagesByKey.entries
        .sortedByDescending { it.key.length }
        .forEach { (key, image) ->
            builder.intercept({ data -> dataMatches(data, key) }, image)
        }
    builder.default(fallback)

    val imageLoader = ImageLoader.Builder(context)
        .components { add(builder.build()) }
        .build()
    SingletonImageLoader.setUnsafe(imageLoader)
}

/** All remote URLs referenced by screenshot sample data. */
fun screenshotRemoteImageUrls(): List<String> = buildList {
    sampleAthletes.forEach { athlete -> athlete.photoUrl?.let(::add) }
    sampleStreams.forEach { add(it.thumbnailUrl) }
    sampleAthleteVideos.forEach { item ->
        add(item.video.thumbnailUrl)
        item.athlete.photoUrl?.let(::add)
    }
}

private fun matchKeysFor(url: String): List<String> {
    val path = url.substringAfter("://").substringAfter('/')
    val last = url.substringAfterLast('/').substringBefore('?')
    return listOf(url, path, last).filter { it.length >= 4 }.distinct()
}

private fun dataMatches(data: Any, key: String): Boolean {
    val s = data.toString()
    if (s == key || s.contains(key)) return true
    // Uri.toString() sometimes differs only by encoding; compare last segment.
    val dataLast = s.substringAfterLast('/').substringBefore('?')
    return dataLast.isNotEmpty() && dataLast == key
}

private fun fixtureCacheDir(): File {
    // Paparazzi unit tests run with module dir (screenshotTests/) as user.dir.
    val moduleBuild = File(System.getProperty("user.dir"), "build/fixture-cache")
    if (moduleBuild.parentFile?.exists() == true || moduleBuild.parentFile?.mkdirs() == true) {
        return moduleBuild
    }
    return File(System.getProperty("java.io.tmpdir"), "betabase-screenshot-fixtures")
}

private fun downloadToImage(url: String, cacheDir: File): Image? {
    val cacheFile = File(cacheDir, cacheKey(url) + extensionFor(url))
    try {
        if (!cacheFile.isFile || cacheFile.length() < 64) {
            fetchToFile(url, cacheFile)
        }
        if (!cacheFile.isFile || cacheFile.length() < 64) return null
        // Paparazzi/Robolectric: decodeFile often returns null; stream decode works.
        val bitmap = cacheFile.inputStream().use { BitmapFactory.decodeStream(it) }
            ?: return null
        return bitmap.asImage()
    } catch (e: Exception) {
        System.err.println("Screenshot fixture error for $url: ${e.message}")
        cacheFile.delete()
        return null
    }
}

private fun fetchToFile(url: String, dest: File) {
    val tmp = File(dest.parentFile, dest.name + ".part")
    val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
        instanceFollowRedirects = true
        connectTimeout = 15_000
        readTimeout = 30_000
        setRequestProperty("User-Agent", "BetabaseScreenshotFixtures/1.0")
        setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
    }
    try {
        val code = connection.responseCode
        if (code !in 200..299) {
            error("HTTP $code")
        }
        connection.inputStream.use { input ->
            tmp.outputStream().use { output -> input.copyTo(output) }
        }
        if (!tmp.renameTo(dest)) {
            tmp.copyTo(dest, overwrite = true)
            tmp.delete()
        }
    } finally {
        connection.disconnect()
        if (tmp.exists()) tmp.delete()
    }
}

private fun cacheKey(url: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
    return digest.joinToString("") { b -> "%02x".format(b) }.take(24)
}

private fun extensionFor(url: String): String = when {
    url.contains("youtube.com") || url.contains("ytimg.com") -> ".jpg"
    url.contains("cloudfront.net") -> ".jpg"
    else -> {
        val ext = url.substringAfterLast('.').substringBefore('?').lowercase()
        if (ext in setOf("jpg", "jpeg", "png", "webp")) ".$ext" else ".bin"
    }
}
