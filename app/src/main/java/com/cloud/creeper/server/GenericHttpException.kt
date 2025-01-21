package com.cloud.creeper.server

import com.yanzhenjie.andserver.error.HttpException

class GenericHttpException: HttpException {

    val code : String

    constructor(code: String, statusCode: Int, message: String): super(statusCode, message) {
        this.code = code
    }

    constructor(code: String, statusCode: Int, cause: Throwable): super(statusCode, cause) {
        this.code = code
    }
}