package com.cloud.spider.protocol.clash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */

@Serializable
data class ClashConfig(
    val port: Int,
    @SerialName("socks-port") val socksPort: Int,
    @SerialName("redir-port") val redirPort: Int,
    @SerialName("tproxy-port") val tproxyPort: Int,
    @SerialName("mixed-port") val mixedPort: Int,
    @SerialName("allow-lan") val allowLan: Boolean,
    @SerialName("bind-address") val bindAddress: String,
    val mode: String,
    @SerialName("log-level") val logLevel: String,
    @SerialName("external-controller") val externalController: String,
    val secret: String,
    @SerialName("external-ui") val externalUI: String,
    val hosts: Map<String, String> = emptyMap(),
    val dns: DNS,
    @SerialName("Proxy") val proxy: List<ClashVMess> = emptyList(),
    @SerialName("Proxy Group") val proxyGroup: List<ProxyGroup>,
    @SerialName("Rule") val rule: List<String>
)
