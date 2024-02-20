package com.cloud.spider.protocol.clash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */
@Serializable
data class ClashVMess(
    val name: String,
    val type: String,
    val server: String,
    val port: String,
    val uuid: String,
    val alterId: Int,
    val cipher: String,
    val tls: Boolean,
    val network: String?,
    @SerialName("ws-path") val wsPath: String?,
    @SerialName("ws-headers") val wsHeaders: Map<String, String>,
    @SerialName("skip-cert-verify") val skipCertVerify: Boolean
)
