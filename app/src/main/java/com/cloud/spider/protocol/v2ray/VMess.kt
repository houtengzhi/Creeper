package com.cloud.spider.protocol.v2ray

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */
@Serializable
data class VMess(
    val v: String,
    val host: String,
    val path: String,
    val tls: String,
    val ps: String,
    val add: String,
    val port: String,
    val id: String,
    val aid: String,
    val net: String,
    val type: String,
    @SerialName("inside_port") val insidePort: String? = null,
    @SerialName("") val unknown: String? = null
)
