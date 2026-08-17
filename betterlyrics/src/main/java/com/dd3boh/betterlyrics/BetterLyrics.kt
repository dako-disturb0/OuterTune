package com.dd3boh.betterlyrics

import com.dd3boh.betterlyrics.models.BeteNodeHealthResult
import com.dd3boh.betterlyrics.models.BeteNodeInterconnectStatus
import com.dd3boh.betterlyrics.models.BeteOriginStatusResponse
import com.dd3boh.betterlyrics.models.TTMLResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object BetterLyrics {
    private const val DEFAULT_API_BASE_URL = "https://lyrics-api.boidu.dev/"
    private const val TTML_LYRICS_PATH = "getLyrics"
    private const val KUGOU_LYRICS_PATH = "kugou/getLyrics"

    private val jsonFormat by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonFormat)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }

            defaultRequest {
                header("User-Agent", "BetterLyrics-OuterTune/1.0")
            }

            expectSuccess = false
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isBlank()) return ""
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }
        return url.trimEnd('/')
    }

    private suspend fun fetchLyrics(
        artist: String,
        title: String,
        album: String?,
        durationSeconds: Int,
        customNodeUrl: String? = null,
        withNodes: Boolean = false,
    ): String? {
        val cleanTitle = title.trim()
        val cleanArtist = artist.trim()
        val cleanAlbum = album?.trim().orEmpty()

        if (cleanTitle.isBlank() || cleanArtist.isBlank()) return null

        // 1. If "With Nodes" is enabled and customNodeUrl is provided, attempt fast edge/origin retrieval first
        if (withNodes && !customNodeUrl.isNullOrBlank()) {
            val nodeBase = normalizeUrl(customNodeUrl)
            if (nodeBase.isNotBlank()) {
                val nodeLyrics = fetchLyricsFromBeteNode(
                    nodeBaseUrl = nodeBase,
                    title = cleanTitle,
                    artist = cleanArtist,
                    album = cleanAlbum,
                    durationSeconds = durationSeconds,
                )
                if (!nodeLyrics.isNullOrBlank()) {
                    return nodeLyrics
                }
            }
        }

        // 2. Standard upstream retrieval as default / fallback
        val endpoints = listOf(TTML_LYRICS_PATH, KUGOU_LYRICS_PATH)
        for (endpoint in endpoints) {
            fetchLyricsFromEndpoint(
                baseUrl = DEFAULT_API_BASE_URL,
                endpoint = endpoint,
                title = cleanTitle,
                artist = cleanArtist,
                album = cleanAlbum,
                durationSeconds = durationSeconds,
            )?.let { lyrics ->
                return lyrics
            }
        }

        return null
    }

    private suspend fun fetchLyricsFromBeteNode(
        nodeBaseUrl: String,
        title: String,
        artist: String,
        album: String,
        durationSeconds: Int,
    ): String? {
        // Strategy A: Try GET /vbeta/lyrics (Fast raw multi-provider endpoint)
        try {
            val vbetaUrl = if (nodeBaseUrl.endsWith("/exec")) {
                "$nodeBaseUrl?path=vbeta/lyrics"
            } else {
                "$nodeBaseUrl/vbeta/lyrics"
            }

            val vbetaResp: HttpResponse = client.get(vbetaUrl) {
                parameter("title", title)
                parameter("author", artist)
                parameter("song", title)
                parameter("artist", artist)
            }

            if (vbetaResp.status.isSuccess()) {
                val body = vbetaResp.bodyAsText().trim()
                if (body.isNotBlank() && !body.contains("\"error\":true")) {
                    extractLyricsFromBody(body)?.let { return it }
                }
            }
        } catch (_: Exception) {}

        // Strategy B: Try POST /v2/lyrics (SSE stream acceleration / cache)
        try {
            val v2Url = if (nodeBaseUrl.endsWith("/exec")) {
                "$nodeBaseUrl?path=v2/lyrics"
            } else {
                "$nodeBaseUrl/v2/lyrics"
            }

            val formParams = Parameters.build {
                append("song", title)
                append("artist", artist)
                if (durationSeconds > 0) {
                    append("duration", durationSeconds.toString())
                }
            }

            val v2Resp: HttpResponse = client.post(v2Url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(formParams))
            }

            if (v2Resp.status.isSuccess()) {
                val body = v2Resp.bodyAsText().trim()
                if (body.isNotBlank() && !body.contains("\"error\":true")) {
                    extractLyricsFromBody(body)?.let { return it }
                }
            }
        } catch (_: Exception) {}

        // Strategy C: Try standard /getLyrics endpoint on node proxy
        try {
            fetchLyricsFromEndpoint(
                baseUrl = nodeBaseUrl,
                endpoint = TTML_LYRICS_PATH,
                title = title,
                artist = artist,
                album = album,
                durationSeconds = durationSeconds,
            )?.let { return it }
        } catch (_: Exception) {}

        return null
    }

    private fun extractLyricsFromBody(body: String): String? {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return null

        // 1. Direct XML / TTML
        if (trimmed.startsWith("<") && (trimmed.contains("<tt") || trimmed.contains("<div"))) {
            return trimmed
        }

        // 2. Direct LRC format (starts with timestamp tag)
        if (trimmed.startsWith("[0") || trimmed.startsWith("[1") || trimmed.startsWith("[2")) {
            return trimmed
        }

        // 3. SSE Stream format (lines starting with 'data:' or 'event:')
        if (trimmed.contains("event:") || trimmed.contains("data:")) {
            val sseExtracted = extractFromSse(trimmed)
            if (!sseExtracted.isNullOrBlank()) return sseExtracted
        }

        // 4. JSON Payload
        if (trimmed.startsWith("{")) {
            try {
                val jsonEl = jsonFormat.parseToJsonElement(trimmed)
                if (jsonEl is kotlinx.serialization.json.JsonObject) {
                    // Check TTML
                    jsonEl["ttml"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                    // Check LRCLIB / syncedLyrics
                    jsonEl["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                    // Check Unison / plain lyrics / lyrics
                    jsonEl["lyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                    jsonEl["plainLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    private fun extractFromSse(sseText: String): String? {
        val lines = sseText.lines()
        val dataLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("data:")) {
                val dataContent = trimmed.substringAfter("data:").trim()
                if (dataContent.isNotBlank() && dataContent != "[DONE]") {
                    dataLines.add(dataContent)
                }
            }
        }

        for (data in dataLines.reversed()) {
            if (data.startsWith("<") && data.contains("<tt")) {
                return data
            }
            if (data.startsWith("{")) {
                try {
                    val obj = jsonFormat.parseToJsonElement(data).jsonObject
                    obj["ttml"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                    obj["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                    obj["lyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let { return it }
                } catch (_: Exception) {}
            }
        }

        return null
    }

    private suspend fun fetchLyricsFromEndpoint(
        baseUrl: String,
        endpoint: String,
        title: String,
        artist: String,
        album: String,
        durationSeconds: Int,
    ): String? {
        return try {
            val base = normalizeUrl(baseUrl)
            val fullUrl = "$base/$endpoint"
            val response: HttpResponse =
                client.get(fullUrl) {
                    parameter("s", title)
                    parameter("a", artist)
                    if (album.isNotBlank()) parameter("al", album)
                    if (durationSeconds > 0) parameter("d", durationSeconds)
                }

            val responseText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return null
            }

            val lyrics =
                try {
                    decodeLyrics(responseText)
                } catch (_: Exception) {
                    ""
                }

            lyrics.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeLyrics(responseText: String): String {
        val trimmed = responseText.trim()
        if (trimmed.startsWith("<")) return trimmed
        return jsonFormat.decodeFromString<TTMLResponse>(responseText).ttml
    }

    suspend fun getLyrics(
        title: String,
        artist: String,
        album: String? = null,
        durationSeconds: Int = -1,
        customNodeUrl: String? = null,
        withNodes: Boolean = false,
    ): Result<String> = runCatching {
        require(title.isNotBlank() && artist.isNotBlank()) { "Song title and artist are required" }
        val ttml =
            fetchLyrics(
                artist = artist,
                title = title,
                album = album,
                durationSeconds = durationSeconds,
                customNodeUrl = customNodeUrl,
                withNodes = withNodes,
            )
                ?: throw IllegalStateException("Lyrics unavailable")
        ttml
    }

    suspend fun getAllLyrics(
        title: String,
        artist: String,
        album: String? = null,
        durationSeconds: Int = -1,
        customNodeUrl: String? = null,
        withNodes: Boolean = false,
        callback: (String) -> Unit,
    ) {
        val result =
            getLyrics(
                title = title,
                artist = artist,
                album = album,
                durationSeconds = durationSeconds,
                customNodeUrl = customNodeUrl,
                withNodes = withNodes,
            )
        result.onSuccess { ttml ->
            callback(ttml)
        }
    }

    /**
     * Inspects a BeteNode (Edge Node or Origin Orchestrator) and returns detailed health & telemetry.
     */
    suspend fun checkNodeHealth(nodeUrl: String): Result<BeteNodeHealthResult> = runCatching {
        val normalized = normalizeUrl(nodeUrl)
        require(normalized.isNotBlank()) { "Invalid Node URL" }

        val start = System.currentTimeMillis()

        // 1. Try /interconnect (Standard Edge Node)
        val isGas = normalized.endsWith("/exec")
        val interconnectUrl = if (isGas) "$normalized?path=interconnect" else "$normalized/interconnect"
        try {
            val resp = client.get(interconnectUrl)
            val latency = System.currentTimeMillis() - start
            if (resp.status.isSuccess()) {
                val text = resp.bodyAsText()
                val parsed = jsonFormat.decodeFromString<BeteNodeInterconnectStatus>(text)
                return@runCatching BeteNodeHealthResult(
                    isOnline = true,
                    latencyMs = latency,
                    role = if (parsed.role.contains("origin", ignoreCase = true)) "Origin Orchestrator" else "Edge Node",
                    platform = parsed.platform,
                    version = parsed.version,
                    hitRate = parsed.cache.hitRate,
                    totalCachedItems = parsed.cache.totalItems,
                    nodeUrl = normalized
                )
            }
        } catch (_: Exception) {}

        // 2. Try /nodes or /status (Origin Orchestrator)
        val nodesUrl = "$normalized/nodes"
        try {
            val resp = client.get(nodesUrl)
            val latency = System.currentTimeMillis() - start
            if (resp.status.isSuccess()) {
                val text = resp.bodyAsText()
                val parsed = jsonFormat.decodeFromString<BeteOriginStatusResponse>(text)
                val roleStr = if (parsed.totalNodes > 0) "Origin Orchestrator (${parsed.totalNodes} nodes)" else "Origin Orchestrator"
                return@runCatching BeteNodeHealthResult(
                    isOnline = true,
                    latencyMs = latency,
                    role = roleStr,
                    platform = parsed.origin?.platform ?: parsed.platform ?: "UNKNOWN",
                    version = "Origin",
                    hitRate = parsed.origin?.cacheStats?.hitRate,
                    totalCachedItems = parsed.origin?.cacheStats?.totalItems,
                    nodeUrl = normalized
                )
            }
        } catch (_: Exception) {}

        // 3. Try /health or /ping (Lightweight ping)
        val healthUrl = if (isGas) "$normalized?path=health" else "$normalized/health"
        try {
            val resp = client.get(healthUrl)
            val latency = System.currentTimeMillis() - start
            if (resp.status.isSuccess()) {
                return@runCatching BeteNodeHealthResult(
                    isOnline = true,
                    latencyMs = latency,
                    role = "Custom Node",
                    platform = "ONLINE",
                    version = null,
                    hitRate = null,
                    totalCachedItems = null,
                    nodeUrl = normalized
                )
            }
        } catch (_: Exception) {}

        throw IllegalStateException("Node connection failed or unreachable")
    }
}
