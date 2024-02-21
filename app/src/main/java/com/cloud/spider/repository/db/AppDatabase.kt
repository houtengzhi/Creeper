package com.cloud.spider.repository.db

import androidx.room.RoomDatabase

/**
 *
 * Created by cloud on 2024/2/21.
 */
abstract class AppDatabase: RoomDatabase() {

    abstract fun appDao(): AppDao
}