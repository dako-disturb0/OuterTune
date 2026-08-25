package app.dkdstrb.excitedtune.ui.utils

// ─────────────────────────────────────────────────────────────────────────────
// YouTube / Google CDN thumbnail URL utilities
// Ported & adapted from ArchiveTune (moe.rukamori.archivetune)
// ─────────────────────────────────────────────────────────────────────────────

private val wHPathRegex    = Regex("w\\d+-h\\d+")
private val wHParamRegex   = Regex("=w(\\d+)-h(\\d+)")
private val sParamRegex    = Regex("=s(\\d+)")
private val brokenSAppendRegex = Regex("-s\\d+")
private val videoIdRegex   = Regex("/vi/([a-zA-Z0-9_-]{11})")

private val DEFAULT_SIZE_BUCKETS = listOf(48, 72, 96, 120, 200, 320, 480, 544, 720, 1080)

private fun getBucketSize(size: Int, buckets: List<Int> = DEFAULT_SIZE_BUCKETS): Int {
    for (bucket in buckets.sorted()) {
        if (size <= bucket) return bucket
    }
    return buckets.max()
}

/** Quality tiers for i.ytimg.com fixed-size thumbnails */
private enum class YtThumbQuality(val value: String) {
    MAXRES("maxresdefault"),
    HQ720("hq720"),
    HQ("hqdefault"),
    MQ("mqdefault"),
    DEFAULT("default"),
}

private fun buildYtimgUrl(videoId: String, quality: YtThumbQuality): String =
    "https://i.ytimg.com/vi/$videoId/${quality.value}.jpg"

private fun chooseYtimgQuality(size: Int, maxresAllowed: Boolean = true): YtThumbQuality = when {
    size <= 120 -> YtThumbQuality.DEFAULT
    size <= 320 -> YtThumbQuality.MQ
    size <= 480 -> YtThumbQuality.HQ
    size <= 720 || !maxresAllowed -> YtThumbQuality.HQ720
    else -> YtThumbQuality.MAXRES
}

/**
 * Resize a YouTube Music / Google CDN thumbnail URL to the requested dimensions.
 *
 * Handles three URL families:
 *  - `lh3.googleusercontent.com` / `ggpht.com`  → rewrite `=w…-h…` / `=s…` params
 *  - `i.ytimg.com/vi/<id>/`                      → swap to an appropriate quality tier
 *  - Everything else                              → returned unchanged
 *
 * When both [width] and [height] are null the original URL is returned.
 */
fun String.resize(
    width: Int? = null,
    height: Int? = null,
    maxresAllowed: Boolean = true,
): String {
    if (width == null && height == null) return this

    val isGoogleCdn = contains("googleusercontent.com") || contains("ggpht.com")
    val isYtimg     = contains("i.ytimg.com")

    // ── Google CDN (lh3 / ggpht) ──────────────────────────────────────────
    if (isGoogleCdn) {
        val rawW = width ?: height!!
        val rawH = height ?: width!!
        val w = getBucketSize(rawW)
        val h = getBucketSize(rawH)

        if (wHPathRegex.containsMatchIn(this)) {
            return replace(wHPathRegex, "w$w-h$h")
        }

        wHParamRegex.find(this)?.let {
            return "${split("=w")[0]}=w$w-h$h-p-l90-rj"
        }

        sParamRegex.find(this)?.let { match ->
            val before = substring(0, match.range.first)
            val after  = substring(match.range.last + 1)
            return "$before=s${maxOf(w, h)}${after.replace(brokenSAppendRegex, "")}"
        }

        return this
    }

    // ── i.ytimg.com fixed-size variants ───────────────────────────────────
    if (isYtimg) {
        val videoId = videoIdRegex.find(this)?.groupValues?.get(1) ?: return this
        val size = maxOf(width ?: 0, height ?: 0)
        return buildYtimgUrl(videoId, chooseYtimgQuality(size, maxresAllowed))
    }

    return this
}

/**
 * Convenience: returns the highest quality version of this thumbnail URL
 * suitable for full-screen player display (1080 px).
 */
fun String.highRes(): String = resize(width = 1080, height = 1080, maxresAllowed = true)