package com.cloud.spider.protocol

/**
 *
 * Created by cloud on 2024/2/19.
 */
enum class ClientType(val value: Int, val text: String) {

    Unknown(100, "Unknown"),
    Clash(200, "Clash"),
    V2Ray(300, "V2Ray");

    fun getClientType(text: String): ClientType = when(text) {
        Clash.text -> Clash
        V2Ray.text -> V2Ray
        else -> Unknown
    }
}