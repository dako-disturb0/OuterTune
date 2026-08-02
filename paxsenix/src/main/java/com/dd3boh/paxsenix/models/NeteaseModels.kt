package com.dd3boh.paxsenix.models

import kotlinx.serialization.Serializable

@Serializable
data class NeteaseSearchResponse(
    val result: NeteaseResult? = null,
)

@Serializable
data class NeteaseResult(
    val songs: List<NeteaseSong> = emptyList(),
)

@Serializable
data class NeteaseSong(
    val id: Long = 0,
    val name: String = "",
    val duration: Int = 0,
)
