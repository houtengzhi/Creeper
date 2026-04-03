package com.cloud.creeper.protocol.clash

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DNS(
    val enable: Boolean,
    val ipv6: Boolean,
    val listen: String,
    @SerialName("enhanced-mode") @EncodeDefault(EncodeDefault.Mode.NEVER) val enhancedMode: String? = null,
    @SerialName("nameserver") val nameServer: List<String>
)
