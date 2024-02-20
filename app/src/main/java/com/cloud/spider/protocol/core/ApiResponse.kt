package com.cloud.spider.protocol.core


/**
 * Created by cloud on 2020-02-12.
 */
sealed interface ApiResponse<out T> {

    data class Success<T>(val data: T) : ApiResponse<T> {
    }
    data class Error(val errorCode: Int, val errorMessage: String) : ApiResponse<Nothing> {

        override fun toString(): String {
            return "Error(errorCode=$errorCode, errorMessage='$errorMessage')"
        }
    }
    data class Exception(val throwable: Throwable) : ApiResponse<Nothing> {
        override fun toString(): String {
            return "Exception(throwable=${throwable.message})"
        }
    }

    companion object {

    }

}