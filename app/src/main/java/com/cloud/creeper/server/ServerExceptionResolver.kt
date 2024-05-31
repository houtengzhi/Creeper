package com.cloud.creeper.server

import com.yanzhenjie.andserver.annotation.Resolver
import com.yanzhenjie.andserver.framework.ExceptionResolver
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse

/**
 *
 * Created by cloud on 2024/4/24.
 */
@Resolver
class ServerExceptionResolver(): ExceptionResolver {
    override fun onResolve(request: HttpRequest, response: HttpResponse, e: Throwable) {

    }
}