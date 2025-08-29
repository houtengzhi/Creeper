package com.cloud.creeper.base

/**
 *
 * Created by cloud on 2024/3/1.
 */
enum class VMError(val errorCode: Int, val errorMessage: String) {

    Unknown(1001, "Unknown error"),

    NotSupportedClientType(1002, "Not supported client type"),

    EmptyProxyList(1003, "Proxy list is empty"),

    SubscriptionSourceNotFound(2000, "Subscription source not found"),

    ConverterNameExisted(3000, "Converter name has been existed");

    override fun toString(): String {
        return "Error(errorCode=$errorCode, errorMessage='$errorMessage')"
    }
}
