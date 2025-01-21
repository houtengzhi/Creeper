package com.cloud.creeper.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionsOutput(@SerialName("subscriptions") val subscriptionList: List<SubscriptionInput>
)
