package com.cloud.creeper.protocol.clash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealityOpts(@SerialName("public-key") val publicKey: String, @SerialName("short-id") val shortId: String)
