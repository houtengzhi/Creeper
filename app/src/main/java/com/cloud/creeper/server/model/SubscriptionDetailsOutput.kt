package com.cloud.creeper.server.model

import com.cloud.creeper.protocol.clash.ClashProxyNode
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubscriptionDetailsOutput(@SerialName("id") @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
                                     @SerialName("source_name") val name: String,
                                     @SerialName("source_url") val sourceUrl: String,
                                     @SerialName("source_type") val type: String,
                                     @SerialName("nodes") val nodeList: List<ClashProxyNode>)
