package com.dd3boh.betterlyrics.models

import kotlinx.serialization.Serializable

@Serializable
data class TTMLResponse(
    val ttml: String = ""
)
