package com.cloud.spider.base


/**
 *
 * Created by cloud on 2021/3/22.
 */
open class DataState<T>(val isLoading: Boolean,
                     val data: T?,
                     val throwable: Throwable?) {

    var error: Error? = null

    constructor(data: T) : this(false, data, null)
    constructor(throwable: Throwable) : this(false, null, throwable)
    constructor(error: Error) : this(false, null, null) {
        this.error = error
    }



    companion object {
        fun <T> initial(): DataState<T> {
            return DataState<T>(isLoading = false, data = null, throwable = null)
        }
    }

    override fun toString(): String {
        return "DataState(isLoading=$isLoading, data=$data, throwable=$throwable, error=$error)"
    }

}
