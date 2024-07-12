package com.cloud.creeper.support

import com.cloud.creeper.protocol.core.ApiResponse
import com.cloud.creeper.repository.Gist
import okhttp3.ResponseBody
import retrofit2.Converter

/**
 *
 * Created by cloud on 2024/7/8.
 */
class GithubConverter: Converter<ResponseBody, ApiResponse<Gist>> {
    override fun convert(p0: ResponseBody): ApiResponse<Gist> {
        TODO("Not yet implemented")
    }

}