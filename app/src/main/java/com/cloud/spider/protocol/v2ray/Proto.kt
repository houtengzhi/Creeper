package com.cloud.spider.protocol.v2ray

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/4/15.
 */
sealed class Proto

@Serializable
data class VMess(
    val v: String,
    val host: String,
    val path: String,
    val tls: String,
    var ps: String,
    //server address
    val add: String,
    val port: String,
    //uuid
    val id: String,
    //alertId
    val aid: String,
    val net: String,
    val type: String,
    @SerialName("inside_port") val insidePort: String? = null,
    @SerialName("") val unknown: String? = null
): Proto() {
    var displayName get() = ps.ifBlank { "${add}:${port}-SS-${hashCode()}" }
        set(value) {
            ps = value
        }
}

data class Trojan(val password: String = "", val server: String = "", val port: String = ""): Proto() {
    var name: String = ""
    var query: String? = null

    var displayName get() = name.ifBlank { "${server}:${port}-SS-${hashCode()}" }
        set(value) {
            name = value
        }
}

data class SS(
    val method: String = "",
    val pwd: String = "",
    val server: String = "",
    val port: String = "",
) : Proto() {
    var name: String = ""

    var displayName get() = name.ifBlank { "${server}:${port}-SS-${hashCode()}" }
        set(value) {
            name = value
        }
}