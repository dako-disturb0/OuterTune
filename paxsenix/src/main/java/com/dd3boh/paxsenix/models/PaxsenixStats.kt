package com.dd3boh.paxsenix.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaxsenixStats(
    @SerialName("uptime_seconds") val uptimeSeconds: Double = 0.0,
    @SerialName("started_at") val startedAt: String = "",
    @SerialName("total_requests") val totalRequests: Int = 0,
    @SerialName("successful_requests") val successfulRequests: Int = 0,
    @SerialName("failed_requests") val failedRequests: Int = 0,
    @SerialName("overall_success_rate") val overallSuccessRate: String = "0%",
    val endpoints: Map<String, EndpointStats> = emptyMap(),
    val providers: Map<String, ProviderStats> = emptyMap(),
    @SerialName("request_log") val requestLog: List<RequestLogEntry> = emptyList(),
)

@Serializable
data class EndpointStats(
    val hits: Int = 0,
    val errors: Int = 0,
    @SerialName("success_rate") val successRate: String = "0%",
    @SerialName("avg_response_time_ms") val avgResponseTimeMs: Double = 0.0,
    @SerialName("last_accessed") val lastAccessed: String = "",
)

@Serializable
data class ProviderStats(
    val hits: Int = 0,
    val errors: Int = 0,
    @SerialName("success_rate") val successRate: String = "0%",
)

@Serializable
data class RequestLogEntry(
    val timestamp: String = "",
    val endpoint: String = "",
    val provider: String = "",
    val success: Boolean = false,
    @SerialName("response_time_ms") val responseTimeMs: Double = 0.0,
    val ip: String = "",
    @SerialName("user_agent") val userAgent: String = "",
)
