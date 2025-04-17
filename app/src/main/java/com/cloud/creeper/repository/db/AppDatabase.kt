package com.cloud.creeper.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cloud.creeper.repository.entity.CloudRepository
import com.cloud.creeper.repository.entity.Converter
import com.cloud.creeper.repository.entity.ConverterCloudRepositoryCrossRef
import com.cloud.creeper.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.entity.SubscriptionSource

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Database(entities = [Converter::class, SubscriptionSource::class, ConverterSubscriptionSourceCrossRef::class,
    ServiceAuth::class, CloudRepository::class, ConverterCloudRepositoryCrossRef::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun appDao(): AppDao
}