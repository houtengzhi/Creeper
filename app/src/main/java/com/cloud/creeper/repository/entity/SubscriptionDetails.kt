package com.cloud.creeper.repository.entity

import com.cloud.creeper.protocol.clash.ClashProxyNode

data class SubscriptionDetails(val subscriptionSource: SubscriptionSource, val nodeList: List<ClashProxyNode>) {

    val nodesMap: Map<String, List<ClashProxyNode>> = mutableMapOf()
}
