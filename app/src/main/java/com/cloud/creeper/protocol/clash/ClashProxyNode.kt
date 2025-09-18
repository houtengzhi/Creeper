package com.cloud.creeper.protocol.clash

import com.cloud.creeper.protocol.base.ProxyNode
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ClashProxyNode(override val name : String, override val type: String, val server: String,
                          val port: String) : ProxyNode {


    @EncodeDefault(EncodeDefault.Mode.NEVER) var password: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var tls: Boolean? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var network: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var uuid: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var alterId: Int? = null

    // ss类型和ssr类型的加密方式：aes-128-gcm、aes-192-gcm、aes-256-gcm、aes-128-cfb、aes-192-cfb、aes-256-cfb、aes-128-ctr、aes-192-ctr、aes-256-ctr、rc4-md5、chacha20-ietf、xchacha20、chacha20-ietf-poly1305、xchacha20-ietf-poly1305
    // vmess类型的加密方式：auto、none、aes-128-gcm、chacha20-poly1305
    @EncodeDefault(EncodeDefault.Mode.NEVER) var cipher: String? = null

    // 协议: origin、auth_sha1_v4、auth_aes128_md5、auth_aes128_sha1、auth_chain_a、auth_chain_b
    @SerialName("protocol") @EncodeDefault(EncodeDefault.Mode.NEVER) var protocol: String? = null

    @SerialName("protocol-param") @EncodeDefault(EncodeDefault.Mode.NEVER) var protocolParam: String? = null

    @SerialName("ws-path") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsPath: String? = null

    @SerialName("ws-headers") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsHeaders: Map<String, String>? = null

    @SerialName("ws-opts") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsOpts: VmessWsOpts? = null

    @SerialName("skip-cert-verify") @EncodeDefault(EncodeDefault.Mode.NEVER) var skipCertVerify: Boolean? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var udp: Boolean? = null

    @SerialName("servername") @EncodeDefault(EncodeDefault.Mode.NEVER) var serverName: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var country: String? = null

    // 混淆方式: plain、http_simple、http_post、random_head、tls1.2_ticket_auth、tls1.2_ticket_fastauth
    @SerialName("obfs") @EncodeDefault(EncodeDefault.Mode.NEVER) var obfs: String? = null

    @SerialName("obfs-param") @EncodeDefault(EncodeDefault.Mode.NEVER) var obfsParam: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var sni: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var smux: Smux? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var flow: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("client-fingerprint") var clientFingerprint: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("reality-opts") var realityOpts: RealityOpts? = null

    /** hysteria2 **/
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("fastOpen") var fastOpen: Boolean? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("up") var up: String? = null  // brutal 速率控制，若不写单位，默认为 Mbps
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("down") var down: String? = null  // brutal 速率控制，若不写单位，默认为 Mbps

    override fun toString(): String {
        return "ClashProxyNode(name='$name', type='$type', server='$server', port='$port', password=$password, tls=$tls, network=$network, uuid=$uuid, alterId=$alterId, cipher=$cipher, wsPath=$wsPath, wsHeaders=$wsHeaders, wsOpts=$wsOpts, skipCertVerify=$skipCertVerify, udp=$udp, serverName=$serverName, country=$country, obfs=$obfs, sni=$sni, smux=$smux, flow=$flow, clientFingerprint=$clientFingerprint, realityOpts=$realityOpts, fastOpen=$fastOpen, up=$up, down=$down)"
    }


}
