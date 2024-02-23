package com.cloud.spider.di

import android.app.Application
import androidx.room.Room
import com.cloud.spider.repository.db.AppDatabase
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.file.FileRepos
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Module
@InstallIn(SingletonComponent::class)
object PersistanceModule {
    const val DATABASE_NAME = "spider_db"

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDbRepos(appDatabase: AppDatabase): DbRepos = DbRepos(appDatabase)

    @Provides
    @Singleton
    fun provideFileRepos(): FileRepos = FileRepos()
}