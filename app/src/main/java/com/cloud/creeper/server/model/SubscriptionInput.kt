package com.cloud.creeper.server.model

import androidx.room.ColumnInfo
import com.cloud.creeper.protocol.ClientType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SubscriptionInput(
    @SerialName("id") @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
    @SerialName("source_name") @EncodeDefault(EncodeDefault.Mode.NEVER) val name: String? = null,
    @SerialName("source_url") @EncodeDefault(EncodeDefault.Mode.NEVER) val sourceUrl: String? = null,
    @SerialName("source_type") @EncodeDefault(EncodeDefault.Mode.NEVER) val type: String? = null
) {
    @SerialName("description")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    var description: String? = null

    override fun toString(): String {
        return "SubscriptionInput(name='$name', sourceUrl='$sourceUrl', type=$type, description=$description)"
    }


}
