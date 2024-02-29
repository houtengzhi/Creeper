package com.cloud.spider.repository.db

import androidx.room.Transaction
import com.cloud.spider.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.spider.repository.entity.ConverterWithSources
import com.cloud.spider.repository.entity.SubscriptionSource

/**
 *
 * Created by cloud on 2024/2/21.
 */
class DbRepos(private val appDatabase: AppDatabase) {

    @Transaction
    suspend fun insertConverter(converter: ConverterWithSources) {
        appDatabase.appDao().insertConverter(converter.converter)
        converter.subscriptionSourceList.forEach {
            appDatabase.appDao().insertConverterSubscriptionSourceCrossRef(
                ConverterSubscriptionSourceCrossRef(converter.converter.id, it.id)
            )
        }
    }

    suspend fun updateConverter(converter: ConverterWithSources) {
        appDatabase.appDao()
    }


    suspend fun queryConverter(name: String): ConverterWithSources? {
        return appDatabase.appDao().queryConverter(name)
    }

    suspend fun queryConverterList(): List<ConverterWithSources> {
        return appDatabase.appDao().queryConverterList()
    }

    suspend fun insertSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().insertSubscriptionSource(source)
    }

    suspend fun updateSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().updateSubscriptionSource(source)
    }

    suspend fun querySubscriptionSourceList() = appDatabase.appDao().querySubscriptionSourceList()
}