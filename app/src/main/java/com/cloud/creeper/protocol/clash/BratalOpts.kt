package com.cloud.creeper.protocol.clash

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BrutalOpts(@EncodeDefault(EncodeDefault.Mode.NEVER) val enabled: Boolean? = null, val up: Int, val down: Int) {
    @EncodeDefault(EncodeDefault.Mode.NEVER) var enable: Boolean? = null
}
