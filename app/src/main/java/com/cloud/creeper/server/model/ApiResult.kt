package com.cloud.creeper.server.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ApiResult<out T>(val code: String, val data: T? = null, @EncodeDefault(EncodeDefault.Mode.NEVER) val message: String? = null)

object ResponseCode {
    const val SUCCESS = "RESPONSE_SUCCESS"
    const val ERROR_FAILED = "RESPONSE_ERROR_FAILED"
    const val ERROR_INVALID_REQUEST = "RESPONSE_ERROR_INVALID_REQUEST"
    const val ERROR_INVALID_TOKEN = "RESPONSE_ERROR_INVALID_TOKEN"
    const val ERROR_PERMISSION = "RESPONSE_ERROR_PERMISSION"
    const val ERROR_NOT_FOUND = "RESPONSE_ERROR_NOT_FOUND"
    const val ERROR_NOT_SERVICE_UNAVAILABLE = "RESPONSE_ERROR_SERVICE_UNAVAILABLE"
}