package com.cloud.creeper.protocol.clash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */

@Serializable
data class DNS(
    val enable: Boolean,
    val ipv6: Boolean,
    val listen: String,
    @SerialName("enhanced-mode") val enhancedMode: String,
    @SerialName("nameserver") val nameServer: List<String>
)
