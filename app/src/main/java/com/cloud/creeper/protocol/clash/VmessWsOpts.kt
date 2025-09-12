package com.cloud.creeper.protocol.clash

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VmessWsOpts(val path: String, @EncodeDefault(EncodeDefault.Mode.NEVER) val headers: Map<String, String>? = null)
