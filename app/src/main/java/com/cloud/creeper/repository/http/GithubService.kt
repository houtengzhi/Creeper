package com.cloud.creeper.repository.http

import com.cloud.creeper.repository.Gist
import com.cloud.creeper.util.SERVICE_GITHUB
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 *
 * Created by cloud on 2024/7/11.
 */
interface GithubService {

    @Headers(*arrayOf("Domain-Name: $SERVICE_GITHUB",
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"))
    @GET("gists")
    suspend fun getGistList(@Header("Authorization") bearerToken: String): List<Gist>

    @Headers(*arrayOf("Domain-Name: $SERVICE_GITHUB",
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"))
    @GET("gists/{gist_id}")
    suspend fun getGist(@Path("gist_id") gistId: String, @Header("Authorization") bearerToken: String): Gist

    @Headers(*arrayOf("Domain-Name: $SERVICE_GITHUB",
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"))
    @POST("gists")
    suspend fun createGist(@Body input: RequestBody, @Header("Authorization") bearerToken: String): Gist

    @Headers(*arrayOf("Domain-Name: $SERVICE_GITHUB",
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"))
    @PATCH("gists/{gist_id}")
    suspend fun updateGist(@Path("gist_id") gistId: String, @Body input: RequestBody, @Header("Authorization") bearerToken: String): Gist

    @Headers(*arrayOf("Domain-Name: $SERVICE_GITHUB",
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"))
    @DELETE("gists/{gist_id}")
    suspend fun deleteGist(@Path("gist_id") gistId: String, @Header("Authorization") bearerToken: String)
}