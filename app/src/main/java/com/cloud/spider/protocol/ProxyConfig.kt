package com.cloud.spider.protocol

import com.cloud.spider.protocol.clash.ClashProxyNode
import com.cloud.spider.protocol.clash.DNS
import com.cloud.spider.protocol.clash.ProxyGroup
import com.cloud.spider.protocol.v2ray.Proto
import com.cloud.spider.protocol.v2ray.SS
import com.cloud.spider.protocol.v2ray.Trojan
import com.cloud.spider.protocol.v2ray.VMess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/4/23.
 */

sealed class ProxyConfig {

    abstract fun toClashConfig(): ClashConfig

    abstract fun toV2RayConfig(): V2RayConfig
}

@Serializable
data class ClashConfig(
    val port: Int? = null,
    @SerialName("socks-port") val socksPort: Int? = null,
    @SerialName("redir-port") val redirPort: Int? = null,
    @SerialName("tproxy-port") val tproxyPort: Int? = null,
    @SerialName("mixed-port") val mixedPort: Int? = null,
    @SerialName("allow-lan") val allowLan: Boolean? = null,
    @SerialName("bind-address") val bindAddress: String? = null,
    val mode: String? = null,
    @SerialName("log-level") val logLevel: String? = null,
    @SerialName("external-controller") val externalController: String? = null,
    val secret: String? = null,
    @SerialName("external-ui") val externalUI: String? = null,
    val hosts: Map<String, String>? = null,
    val dns: DNS? = null,
    @SerialName("proxies") val proxies: List<ClashProxyNode>? = null,
    @SerialName("proxy-groups") val proxyGroup: List<ProxyGroup>? = null,
    @SerialName("rules") val rules: List<String>? = null
): ProxyConfig() {

    override fun toClashConfig(): ClashConfig {
        return this
    }

    override fun toV2RayConfig(): V2RayConfig {
        val protoList = mutableListOf<Proto>()
        this.proxies?.forEach { node ->
            when(node.type) {
                "vmess" -> {

                }
                "ss" -> {

                }
                "trojan" -> {

                }
            }
        }
        return V2RayConfig(protoList)
    }
}

data class V2RayConfig(val protoList: List<Proto>) : ProxyConfig() {
    override fun toClashConfig(): ClashConfig {
        val clashProxyNodeList = mutableListOf<ClashProxyNode>()
        this.protoList.forEach {proto ->
            val clashProxyNode: ClashProxyNode
            when (proto) {
                is VMess -> {
                    clashProxyNode = ClashProxyNode(
                        name = proto.ps,
                        type = "vmess",
                        server = proto.add,
                        port = proto.port,
                    ).apply {
                        tls = proto.tls != ""
                        uuid = proto.id
                        alterId = proto.aid.toInt()
                        cipher = proto.type
                        network = if (proto.net == "ws") "ws" else null
                        wsPath = if (proto.net == "ws") proto.path else null
                        wsHeaders = mapOf("Host" to proto.host)
                        skipCertVerify = true
                    }
                }

                is SS -> {
                    clashProxyNode = ClashProxyNode(
                        name = proto.name,
                        type = "ss",
                        server = proto.server,
                        port = proto.port,

                        ).apply {
                        password = proto.pwd
                        cipher = proto.method
                    }
                }

                is Trojan -> {
                    clashProxyNode = ClashProxyNode(
                        name = proto.displayName,
                        type = "trojan",
                        server = proto.server,
                        port = proto.port,
                    ).apply {
                        password = proto.password
                    }
                }
            }
            clashProxyNodeList.add(clashProxyNode)

        }
        return ClashConfig(proxies = clashProxyNodeList)
    }

    override fun toV2RayConfig(): V2RayConfig {
        return this
    }
}