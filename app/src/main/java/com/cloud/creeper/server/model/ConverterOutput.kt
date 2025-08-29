package com.cloud.creeper.server.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ConverterOutput(@SerialName("id") val id: String, @SerialName("name") val name: String, @SerialName("output_type") val outputType: String) {

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("description")
    var description: String? = null

    @SerialName("created_time")
    var createdTime: Long = 0

    @SerialName("updated_time")
    var updatedTime: Long = 0
}
