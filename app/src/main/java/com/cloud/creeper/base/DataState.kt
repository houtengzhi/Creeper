package com.cloud.creeper.base

import com.cloud.creeper.protocol.core.ApiResponse


/**
 *
 * Created by cloud on 2021/3/22.
 */
open class DataState<T>(val isLoading: Boolean,
                     val data: T?,
                     val throwable: Throwable?) {

    var vmError: VMError? = null
    var error: ApiResponse.Error? = null

    constructor(data: T) : this(false, data, null)
    constructor(throwable: Throwable) : this(false, null, throwable)
    constructor(error: VMError) : this(false, null, null) {
        this.error = ApiResponse.Error(error)
    }
    constructor(error: ApiResponse.Error) : this(false, null, null) {
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
