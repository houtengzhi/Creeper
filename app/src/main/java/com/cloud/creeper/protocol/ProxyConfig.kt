package com.cloud.creeper.protocol

import com.cloud.creeper.protocol.clash.ClashProxyNode
import com.cloud.creeper.protocol.clash.DNS
import com.cloud.creeper.protocol.clash.ProxyGroup
import com.cloud.creeper.protocol.clash.VmessWsOpts
import com.cloud.creeper.protocol.v2ray.Proto
import com.cloud.creeper.protocol.v2ray.SS
import com.cloud.creeper.protocol.v2ray.Trojan
import com.cloud.creeper.protocol.v2ray.VMess
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ClashConfig(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val port: Int? = null,
    @SerialName("socks-port") @EncodeDefault(EncodeDefault.Mode.NEVER) val socksPort: Int? = null,
    @SerialName("redir-port") @EncodeDefault(EncodeDefault.Mode.NEVER) val redirPort: Int? = null,
    @SerialName("tproxy-port") @EncodeDefault(EncodeDefault.Mode.NEVER) val tproxyPort: Int? = null,
    @SerialName("mixed-port") @EncodeDefault(EncodeDefault.Mode.NEVER) val mixedPort: Int? = null,
    @SerialName("allow-lan") @EncodeDefault(EncodeDefault.Mode.NEVER) val allowLan: Boolean? = null,
    @SerialName("bind-address") @EncodeDefault(EncodeDefault.Mode.NEVER) val bindAddress: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val mode: String? = null,
    @SerialName("log-level") @EncodeDefault(EncodeDefault.Mode.NEVER) val logLevel: String? = null,
    @SerialName("external-controller") @EncodeDefault(EncodeDefault.Mode.NEVER) val externalController: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val secret: String? = null,
    @SerialName("external-ui") @EncodeDefault(EncodeDefault.Mode.NEVER) val externalUI: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val hosts: Map<String, String>? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val dns: DNS? = null,
    @SerialName("proxies") @EncodeDefault(EncodeDefault.Mode.NEVER) val proxies: List<ClashProxyNode>? = null,
    @SerialName("proxy-groups") @EncodeDefault(EncodeDefault.Mode.NEVER) val proxyGroup: List<ProxyGroup>? = null,
    @SerialName("rules") @EncodeDefault(EncodeDefault.Mode.NEVER) val rules: List<String>? = null
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
                        cipher = if (proto.type == "none") "auto" else null
                        network = if (proto.net == "ws") "ws" else null
//                        wsPath = if (proto.net == "ws") proto.path else null
//                        wsHeaders = mapOf("Host" to proto.host)
                        wsOpts = VmessWsOpts(if (proto.net == "ws") proto.path else "", mapOf("Host" to proto.host))
                        skipCertVerify = false
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