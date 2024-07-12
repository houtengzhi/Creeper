package com.cloud.creeper.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/7/11.
 */
@Serializable
data class GistFile(
    @SerialName("filename") val fileName: String,
    val type: String,
    val language: String,
    val size: Int,
    @SerialName("raw_url") val rawUrl: String,
    val content: String
)
