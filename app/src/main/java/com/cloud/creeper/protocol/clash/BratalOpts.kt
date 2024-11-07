package com.cloud.creeper.protocol.clash

import kotlinx.serialization.Serializable

@Serializable
data class BrutalOpts(val enable: Boolean, val up: Int, val down: Int)
