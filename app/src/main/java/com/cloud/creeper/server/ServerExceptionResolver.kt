package com.cloud.creeper.server

import com.cloud.creeper.server.model.ApiResult
import com.cloud.creeper.server.model.ResponseCode
import com.yanzhenjie.andserver.annotation.Resolver
import com.yanzhenjie.andserver.error.HttpException
import com.yanzhenjie.andserver.framework.ExceptionResolver
import com.yanzhenjie.andserver.framework.body.JsonBody
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.StatusCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 *
 * Created by cloud on 2024/4/24.
 */
@Resolver
class ServerExceptionResolver(): ExceptionResolver {
    override fun onResolve(request: HttpRequest, response: HttpResponse, e: Throwable) {
        val apiResult = when (e) {
            is GenericHttpException -> {
                response.status = e.statusCode
                ApiResult(code = e.code, message = e.message)

            }

            is HttpException -> {
                response.status = e.statusCode
                ApiResult(code = ResponseCode.ERROR_FAILED, message = e.message)

            }

            else -> {
                response.status = StatusCode.SC_INTERNAL_SERVER_ERROR
                ApiResult<Nothing>(code = ResponseCode.ERROR_FAILED, message = e.message)
            }
        }
        response.setBody(JsonBody(Json.Default.encodeToString(apiResult)))
    }
}