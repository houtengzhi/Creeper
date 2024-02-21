package com.cloud.spider.di

import android.app.Application
import com.cloud.spider.base.SpiderApp
import com.yanzhenjie.andserver.AndServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *
 * Created by cloud on 2024/1/30.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideSpiderApplication(application: Application): SpiderApp = application as SpiderApp
}