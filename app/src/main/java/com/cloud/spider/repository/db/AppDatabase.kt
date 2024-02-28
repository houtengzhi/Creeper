package com.cloud.spider.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cloud.spider.repository.entity.Converter
import com.cloud.spider.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.spider.repository.entity.SubscriptionSource

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Database(entities = [Converter::class, SubscriptionSource::class, ConverterSubscriptionSourceCrossRef::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun appDao(): AppDao
}