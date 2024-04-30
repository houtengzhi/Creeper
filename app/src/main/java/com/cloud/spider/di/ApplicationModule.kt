package com.cloud.spider.di

import android.app.Application
import com.cloud.spider.base.SpiderApp
import com.cloud.spider.repository.DataRepos
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
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

    @Provides
    @Singleton
    fun provideDataRepos(httpRepos: HttpRepos, dbRepos: DbRepos, fileRepos: FileRepos): DataRepos {
        return DataRepos(httpRepos, dbRepos, fileRepos)
    }
}