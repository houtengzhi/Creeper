package com.cloud.creeper.protocol.clash

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RealityOpts(
    @SerialName("public-key") val publicKey: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("short-id") val shortId: String? = null
)
