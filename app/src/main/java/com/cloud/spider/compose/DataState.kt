package com.cloud.spider.compose


/**
 *
 * Created by cloud on 2021/3/22.
 */
open class DataState<T>(val isLoading: Boolean,
                     val data: T?,
                     val throwable: Throwable?) {

    var errorCode: Int = 0
    var errorMessage: String? = null

    constructor(data: T) : this(false, data, null)
    constructor(throwable: Throwable) : this(false, null, throwable)
    constructor(errorCode: Int, errorMessage: String) : this(false, null, null) {
        this.errorCode = errorCode
        this.errorMessage = errorMessage
    }

    companion object {
        fun <T> initial(): DataState<T> {
            return DataState<T>(isLoading = false, data = null, throwable = null)
        }
    }

}
