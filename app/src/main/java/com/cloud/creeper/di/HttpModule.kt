package com.cloud.creeper.di

import com.cloud.creeper.repository.http.GithubService
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.util.GITHUB_BASE_URL
import com.cloud.creeper.util.SERVICE_GITHUB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Module
@InstallIn(SingletonComponent::class)
object HttpModule {

    @Provides
    @Singleton
    fun createOkHttpClient(): OkHttpClient {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        builder.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            })
        RetrofitUrlManager.getInstance().putDomain(SERVICE_GITHUB, GITHUB_BASE_URL)
        return RetrofitUrlManager.getInstance().with(builder).build()
    }

    @Provides
    @Singleton
    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit.Builder {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=utf-8".toMediaType()))
    }


    @Provides
    @Singleton
    fun createGithubService(builder: Retrofit.Builder): GithubService {
        val retrofit = builder.build()
        return retrofit.create(GithubService::class.java)
    }

    @Provides
    @Singleton
    fun createHttpRepos(httpClient: OkHttpClient, githubService: GithubService): HttpRepos =
        HttpRepos(httpClient, githubService)
}