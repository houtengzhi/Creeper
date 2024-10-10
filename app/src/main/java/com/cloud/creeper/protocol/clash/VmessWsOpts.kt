package com.cloud.creeper.protocol.clash

import kotlinx.serialization.Serializable

@Serializable
data class VmessWsOpts(val path: String, val headers: Map<String, String>)
