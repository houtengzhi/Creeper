package com.cloud.creeper.protocol

/**
 *
 * Created by cloud on 2024/2/19.
 */
enum class ClientType(val value: Int) {

    Unknown(100),
    Clash(200),
    ClashMeta(210),
    V2Ray(300);

}