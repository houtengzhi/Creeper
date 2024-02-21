package com.cloud.spider.protocol

/**
 *
 * Created by cloud on 2024/2/19.
 */
enum class ClientType(val value: Int, val text: String) {

    Clash(100, "Clash"),
    V2Ray(200, "V2Ray");

    fun getClientType(text: String): ClientType = when(text) {
        Clash.text -> Clash
        V2Ray.text -> V2Ray
        else -> Clash
    }
}