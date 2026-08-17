package com.dd3boh.betterlyrics.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BeteNodeCacheStats(
    @SerialName("total_items") val totalItems: Int = 0,
    @SerialName("max_items") val maxItems: Int = 0,
    val hits: Long = 0,
    val misses: Long = 0,
    @SerialName("hit_rate") val hitRate: String = "0%",
    val provider: String? = null
)

@Serializable
data class BeteNodeInterconnectStatus(
    val status: String = "unknown",
    val role: String = "edge_node",
    val version: String = "1.0.0",
    val platform: String = "UNKNOWN",
    @SerialName("uptime_sec") val uptimeSec: Long = 0,
    val goroutines: Int = 0,
    @SerialName("memory_mb") val memoryMb: Double = 0.0,
    val cache: BeteNodeCacheStats = BeteNodeCacheStats(),
    val timestamp: Long = 0,
    @SerialName("server_time") val serverTime: String = ""
)

@Serializable
data class BeteOriginInfo(
    val role: String = "origin_orchestrator",
    val platform: String = "UNKNOWN",
    val upstream: String = "",
    @SerialName("uptime_sec") val uptimeSec: Long = 0,
    @SerialName("cache_stats") val cacheStats: BeteNodeCacheStats = BeteNodeCacheStats()
)

@Serializable
data class BeteNodePoolItem(
    val id: Int = 0,
    @SerialName("base_url") val baseUrl: String = "",
    val healthy: Boolean = false,
    @SerialName("latency_ms") val latencyMs: Long = 0,
    @SerialName("fail_count") val failCount: Int = 0
)

@Serializable
data class BeteOriginStatusResponse(
    val origin: BeteOriginInfo? = null,
    @SerialName("total_nodes") val totalNodes: Int = 0,
    val nodes: List<BeteNodePoolItem> = emptyList(),
    val service: String? = null,
    val status: String? = null,
    val platform: String? = null
)

data class BeteNodeHealthResult(
    val isOnline: Boolean,
    val latencyMs: Long,
    val role: String, // "Edge Node", "Origin Orchestrator", "Google Apps Script", "Custom Node"
    val platform: String,
    val version: String?,
    val hitRate: String?,
    val totalCachedItems: Int?,
    val error: String? = null,
    val nodeUrl: String
)
