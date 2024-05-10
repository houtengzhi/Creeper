package com.cloud.spider.protocol.clash

import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/2/19.
 */
@Serializable
data class ProxyGroup(
    val name: String,
    val type: String,
    val proxies: List<String>,
    val url: String? = null,
    val interval: Int? = null,
    val tolerance: Int? = null
)
