package com.cloud.creeper.server.model

import com.cloud.creeper.repository.entity.SourceStatus
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubscriptionOutput(@SerialName("id") @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
                         @SerialName("source_name") @EncodeDefault(EncodeDefault.Mode.NEVER) val name: String? = null,
                         @SerialName("source_url") @EncodeDefault(EncodeDefault.Mode.NEVER) val sourceUrl: String? = null,
                         @SerialName("source_type") @EncodeDefault(EncodeDefault.Mode.NEVER) val type: String? = null) {

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("description")
    var description: String? = null

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("source_icon_path")
    var iconPath: String? = null

    @SerialName("created_time")
    var createdTime: Long = 0

    @SerialName("updated_time")
    var updatedTime: Long = 0

    @SerialName("pull_status")
    var pullStatus: SourceStatus = SourceStatus.IDLE
}