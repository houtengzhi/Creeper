package com.cloud.creeper.di

import android.app.Application
import com.cloud.creeper.base.CreeperApp
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
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
    fun provideCreeperApplication(application: Application): CreeperApp = application as CreeperApp

    @Provides
    @Singleton
    fun provideDataRepos(httpRepos: HttpRepos, dbRepos: DbRepos, fileRepos: FileRepos): DataRepos {
        return DataRepos(httpRepos, dbRepos, fileRepos)
    }
}