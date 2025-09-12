package com.cloud.creeper.support.filter

import com.cloud.creeper.protocol.ProxyConfig

interface IProxyFilter {

    fun isExcluded(proxy : ProxyConfig): Boolean
}