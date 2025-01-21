package com.cloud.creeper.protocol.core

import com.cloud.creeper.base.VMError
import com.cloud.creeper.server.model.ApiResult


/**
 * Created by cloud on 2020-02-12.
 */
sealed interface ApiResponse<out T> {

    data class Success<T>(val data: T) : ApiResponse<T> {
    }
    data class Error(val errorCode: Int, val errorMessage: String) : ApiResponse<Nothing> {

        constructor(error: Error): this(error.errorCode, error.errorMessage)

        constructor(error: VMError) : this(error.errorCode, error.errorMessage)

        override fun toString(): String {
            return "Error(errorCode=$errorCode, errorMessage='$errorMessage')"
        }
    }
    data class Exception(val throwable: Throwable) : ApiResponse<Nothing> {

        constructor(exception: Exception): this(exception.throwable)

        override fun toString(): String {
            return "Exception(throwable=${throwable.message})"
        }
    }

    companion object {

    }

    fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> ApiResult(
                code = "200",
                data = transform(this.data)
            )
            is Error -> ApiResult(
                code = this.errorCode.toString(),
                message = this.errorMessage
            )
            is Exception -> ApiResult(
                code = "500",
                message = this.throwable.message
            )
        }
    }
}