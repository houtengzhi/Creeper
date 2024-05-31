package com.cloud.creeper.protocol.core

import com.charleskorn.kaml.Yaml
import com.cloud.creeper.protocol.ClashConfig
import com.cloud.creeper.protocol.V2RayConfig
import com.cloud.creeper.protocol.clash.ClashProxyNode
import com.cloud.creeper.protocol.v2ray.Proto
import com.cloud.creeper.protocol.v2ray.SS
import com.cloud.creeper.protocol.v2ray.Trojan
import com.cloud.creeper.protocol.v2ray.VMess
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 *
 * Created by cloud on 2024/2/19.
 */
object ConverterUtil {

    private val REG_SCHEMA_HASH = "(\\w+)://([^ #]+)(?:#([^#]+)?)?".toRegex()
    private val REG_SS = "([^:]+):([^@]+)@([^:]+):(\\d{1,5})/?".toRegex()
    private val REG_SSR_PARAM = "([^/]+)/\\?(.+)".toRegex()
    private val REG_TROJAN = "([^@]+)@([^:]+):(\\d{1,5})/?(?:\\?(.+))?".toRegex()

    const val TAG = "ConverterUtil"

    fun deserializeClashConfig(content: String): ClashConfig {
        return Yaml.default.decodeFromString(ClashConfig.serializer(), content)
    }

    fun serializeClashConfig(clashConfig: ClashConfig): String {
        return Yaml.default.encodeToString(ClashConfig.serializer(), clashConfig)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun readV2RaySubscription(content: String): V2RayConfig {
        val protoList = mutableListOf<Proto>()
        Base64.decode(content).decodeToString().split("\n")
            .map { split ->
                if (split.startsWith("vmess://")) {
                    val json = Base64.decode(split.removePrefix("vmess://")).decodeToString()
                    val vmess = Json.decodeFromString<VMess>(json)
                    protoList.add(vmess)

                } else if (split.startsWith("trojan://")) {
                    REG_SCHEMA_HASH.matchEntire(split)?.run {
                        val name = URLDecoder.decode(groupValues[3], "UTF-8")
                        groupValues[2].also {
                            REG_TROJAN.matchEntire(it)?.run {
                                 val trojan = Trojan(groupValues[1], groupValues[2], groupValues[3]).apply {
                                    this.name = name
                                    this.query = groupValues[4]
                                }
                                protoList.add(trojan)
                            }
                        }
                    }

                } else if (split.startsWith("ss://")) {
                    REG_SCHEMA_HASH.matchEntire(split)?.run {
                        val name = URLDecoder.decode(groupValues[3], "UTF-8")
                        val decoded =
                            groupValues[2].takeUnless { it.contains("@") }?.b64Decode()
                            // 兼容异常
                                ?: with(groupValues[2]) {
                                    "${substringBefore('@').b64Decode()}${substring(indexOf('@'))}"
                                }
                        decoded.also {
                            REG_SS.matchEntire(it)?.run {
                                val ss = SS(groupValues[1], groupValues[2], groupValues[3], groupValues[4])
                                    .apply { this.name = name }
                                protoList.add(ss)
                            }
                        }
                    }
                } else {

                }
            }
        return V2RayConfig(protoList)
    }

    fun toClashConfig(clashConfig: ClashConfig?, v2rayProtoList: List<Proto>?): ClashConfig {

        val clashProxyNodeList = mutableListOf<ClashProxyNode>()
        clashConfig?.let { config ->
            config.proxies?.let {
                clashProxyNodeList.addAll(it)
            }
        }
        v2rayProtoList?.forEach { proto ->
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

}