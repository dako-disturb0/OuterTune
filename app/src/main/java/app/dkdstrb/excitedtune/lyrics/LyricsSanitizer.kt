package app.dkdstrb.excitedtune.lyrics

object LyricsSanitizer {

    private val PREFIXES = listOf(
        "e:",
        "local:",
        "account:",
        "pinned:",
        "modified:",
        "monthly:"
    )

    private val NOISE_PATTERNS = listOf(
        Regex("""(?i)\s*[\(\[](?:official\s+(?:music\s+)?video|official\s+audio|lyric\s+video|official\s+lyric\s+video|audio|visualizer|mv|hd|4k|explicit|clean|remastered(?:\s+\d{4})?)[\)\]]"""),
        Regex("""(?i)\s*[\(\[]feat\.\s+[^\]\)]+[\)\]]"""),
        Regex("""(?i)\s*[\(\[]ft\.\s+[^\]\)]+[\)\]]"""),
        Regex("""(?i)\s*[\(\[]with\s+[^\]\)]+[\)\]]""")
    )

    /**
     * Cleans a track title for lyrics searching by removing internal prefixes, explicit icons,
     * and extraneous media suffixes like "(Official Video)" or "(feat. Artist)".
     */
    fun cleanTitle(title: String): String {
        var cleaned = title.trim()
        for (prefix in PREFIXES) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
            }
        }

        // Remove explicit symbol emoji
        cleaned = cleaned.replace("\uD83C\uDD74", "").trim()

        // Remove video/audio Noise
        for (pattern in NOISE_PATTERNS) {
            cleaned = pattern.replace(cleaned, "")
        }

        return cleaned.trim().ifBlank { title }
    }

    /**
     * Cleans an artist name for lyrics searching by removing internal prefixes.
     */
    fun cleanArtist(artist: String): String {
        var cleaned = artist.trim()
        for (prefix in PREFIXES) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
            }
        }
        return cleaned.trim().ifBlank { artist }
    }
}
