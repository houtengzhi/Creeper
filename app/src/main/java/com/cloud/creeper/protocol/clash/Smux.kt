package com.cloud.creeper.protocol.clash

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Smux(val enabled: Boolean) {
    @EncodeDefault(EncodeDefault.Mode.NEVER) var protocol: String? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("max-connections") @JsonNames("max_connections") var maxConnections: Int? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("min-streams") var minStreams: Int? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("max-streams") var maxStreams: Int? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) var statistic: Boolean? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("only-tcp") var onlyTcp: Boolean? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) var padding: Boolean? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("brutal-opts") var brutalOpts: BrutalOpts? = null
}
