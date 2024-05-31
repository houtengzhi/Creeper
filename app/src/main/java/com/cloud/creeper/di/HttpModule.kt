package com.cloud.creeper.di

import com.cloud.creeper.repository.http.HttpRepos
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
        return builder.build()
    }

    @Provides
    @Singleton
    fun createHttpRepos(httpClient: OkHttpClient): HttpRepos =
        HttpRepos(httpClient)
}