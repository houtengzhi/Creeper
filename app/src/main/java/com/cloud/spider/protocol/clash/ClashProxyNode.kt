package com.cloud.spider.protocol.clash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */
@Serializable
data class ClashProxyNode(
    val name: String,
    val type: String,
    val server: String,
    val port: String
) {
    var password: String? = null

    var tls: Boolean? = null

    var network: String? = null

    var uuid: String? = null

    var alterId: Int? = null

    var cipher: String? = null

    @SerialName("ws-path") var wsPath: String? = null

    @SerialName("ws-headers") var wsHeaders: Map<String, String>? = null

    @SerialName("skip-cert-verify") var skipCertVerify: Boolean? = null

    var udp: Boolean? = null
}
