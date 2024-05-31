package com.cloud.creeper.repository.db

import android.util.Log
import androidx.room.Transaction
import com.cloud.creeper.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.SubscriptionSource

/**
 *
 * Created by cloud on 2024/2/21.
 */
class DbRepos(private val appDatabase: AppDatabase) {

    companion object {
        private const val TAG = "DbRepos"
    }

    @Transaction
    suspend fun suspendInsertConverter(converter: ConverterWithSources) {
        Log.d(TAG, "suspendInsertConverter()")
        val dao = appDatabase.appDao()
        dao.suspendInsertConverter(converter.converter)
        converter.subscriptionSourceList.forEach {
            dao.suspendInsertConverterSubscriptionSourceCrossRef(
                ConverterSubscriptionSourceCrossRef(converter.converter.id, it.id)
            )
        }
    }

    @Transaction
    suspend fun suspendUpdateConverter(converter: ConverterWithSources) {
        val dao = appDatabase.appDao()
        dao.suspendUpdateConverter(converter.converter)
        dao.suspendDeleteConverterSubscriptionSourceCrossRefByConverterId(converter.converter.id)
        converter.subscriptionSourceList.forEach {
            dao.suspendInsertConverterSubscriptionSourceCrossRef(
                ConverterSubscriptionSourceCrossRef(converter.converter.id, it.id)
            )
        }
    }

    @Transaction
    suspend fun suspendDeleteConverter(converter: ConverterWithSources) {
        val dao = appDatabase.appDao()
        dao.suspendDeleteConverter(converter.converter)
        dao.suspendDeleteConverterSubscriptionSourceCrossRefByConverterId(converter.converter.id)
    }

    suspend fun suspendQueryConverter(name: String): ConverterWithSources? {
        return appDatabase.appDao().suspendQueryConverterByName(name)
    }

    fun queryConverterByName(name: String): ConverterWithSources? {
        return appDatabase.appDao().queryConverterByName(name)
    }

    fun queryConverterById(converterId: String): ConverterWithSources? {
        return appDatabase.appDao().queryConverterById(converterId)
    }

    suspend fun queryConverterList(): List<ConverterWithSources> {
        return appDatabase.appDao().queryConverterList()
    }

    fun subscribeConverterList() = appDatabase.appDao().subscribeConverterList()

    suspend fun insertSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().insertSubscriptionSource(source)
    }

    suspend fun updateSubscriptionSource(source: SubscriptionSource) {
        Log.d(TAG, "updateSubscriptionSource()")
        appDatabase.appDao().updateSubscriptionSource(source)
    }

    suspend fun deleteSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().deleteSubscriptionSource(source)
    }

    suspend fun querySubscriptionSourceList() = appDatabase.appDao().querySubscriptionSourceList()

    fun subscribeSubscriptionSourceList() = appDatabase.appDao().subscribeSubscriptionSourceList()
}