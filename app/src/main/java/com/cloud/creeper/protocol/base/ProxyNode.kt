package com.cloud.creeper.protocol.base

import kotlinx.serialization.Serializable


interface ProxyNode {
    val name: String
    val type: String
}
