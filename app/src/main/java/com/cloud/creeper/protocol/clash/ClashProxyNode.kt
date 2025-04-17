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

    @EncodeDefault(EncodeDefault.Mode.NEVER) var cipher: String? = null

    @SerialName("ws-path") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsPath: String? = null

    @SerialName("ws-headers") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsHeaders: Map<String, String>? = null

    @SerialName("ws-opts") @EncodeDefault(EncodeDefault.Mode.NEVER) var wsOpts: VmessWsOpts? = null

    @SerialName("skip-cert-verify") @EncodeDefault(EncodeDefault.Mode.NEVER) var skipCertVerify: Boolean? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var udp: Boolean? = null

    @SerialName("servername") @EncodeDefault(EncodeDefault.Mode.NEVER) var serverName: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var country: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var obfs: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var sni: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var smux: Smux? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) var flow: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("client-fingerprint") var clientFingerprint: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("reality-opts") var realityOpts: RealityOpts? = null

    /** hysteria2 **/
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("fastOpen") var fastOpen: Boolean? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("up") var up: Int? = null
    @EncodeDefault(EncodeDefault.Mode.NEVER) @SerialName("down") var down: Int? = null

    override fun toString(): String {
        return "ClashProxyNode(name='$name', type='$type', server='$server', port='$port', password=$password, tls=$tls, network=$network, uuid=$uuid, alterId=$alterId, cipher=$cipher, wsPath=$wsPath, wsHeaders=$wsHeaders, wsOpts=$wsOpts, skipCertVerify=$skipCertVerify, udp=$udp, serverName=$serverName, country=$country, obfs=$obfs, sni=$sni, smux=$smux, flow=$flow, clientFingerprint=$clientFingerprint, realityOpts=$realityOpts, fastOpen=$fastOpen, up=$up, down=$down)"
    }


}
