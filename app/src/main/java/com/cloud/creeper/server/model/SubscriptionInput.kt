package com.cloud.creeper.server.model

import androidx.room.ColumnInfo
import com.cloud.creeper.protocol.ClientType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubscriptionInput(@SerialName("source_name") val name: String,
                             @SerialName("source_url") val sourceUrl: String,
                             @SerialName("source_type") val type: String
) {
    @SerialName("description") @EncodeDefault(EncodeDefault.Mode.NEVER)
    var description: String? = null

    override fun toString(): String {
        return "SubscriptionInput(name='$name', sourceUrl='$sourceUrl', type=$type, description=$description)"
    }


}
