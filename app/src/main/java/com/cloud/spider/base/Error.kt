package com.cloud.spider.base

/**
 *
 * Created by cloud on 2024/3/1.
 */
data class Error(val errorCode: Int, val errorMessage: String) {

    override fun toString(): String {
        return "Error(errorCode=$errorCode, errorMessage='$errorMessage')"
    }
}
