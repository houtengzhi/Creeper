package com.cloud.creeper.base

/**
 *
 * Created by cloud on 2024/3/1.
 */
enum class AppError(val errorCode: Int, val errorMessage: String) {

    Unknown(-900, "Unknown error"),

    HttpError(-100, "Http request failed"),

    NotSupportedClientType(-200, "Not supported client type"),

    EmptyProxyList(-201, "Proxy list is empty"),

    SubscriptionSourceNotFound(-301, "Subscription source not found"),

    ConverterNameExisted(-302, "Converter name has been existed");

    override fun toString(): String {
        return "Error(errorCode=$errorCode, errorMessage='$errorMessage')"
    }
}
