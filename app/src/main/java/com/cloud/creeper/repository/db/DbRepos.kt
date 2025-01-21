package com.cloud.creeper.repository.db

import android.util.Log
import androidx.room.Transaction
import com.cloud.creeper.repository.entity.CloudRepository
import com.cloud.creeper.repository.entity.ConverterCloudRepositoryCrossRef
import com.cloud.creeper.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
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
        Log.d(TAG, "suspendInsertConverter() $converter")
        val dao = appDatabase.appDao()
        dao.suspendInsertConverter(converter.converter)
        converter.subscriptionSourceList.forEach {
            dao.suspendInsertConverterSubscriptionSourceCrossRef(
                ConverterSubscriptionSourceCrossRef(converter.converter.id, it.id)
            )
        }
        converter.cloudRepositoryList?.forEach {
            dao.suspendInsertCloudRepository(it)
            dao.suspendInsertConverterCloudRepositoryCrossRef(ConverterCloudRepositoryCrossRef(converter.converter.id, it.id))
        }
    }

    @Transaction
    suspend fun suspendUpdateConverter(converter: ConverterWithSources) {
        Log.d(TAG, "suspendUpdateConverter() $converter")
        val dao = appDatabase.appDao()
        dao.suspendUpdateConverter(converter.converter)
        dao.suspendDeleteConverterSubscriptionSourceCrossRefByConverterId(converter.converter.id)
        converter.subscriptionSourceList.forEach {
            dao.suspendInsertConverterSubscriptionSourceCrossRef(
                ConverterSubscriptionSourceCrossRef(converter.converter.id, it.id)
            )
        }
        dao.suspendDeleteConverterCloudRepositoryCrossRefByConverterId(converter.converter.id)
        converter.cloudRepositoryList?.forEach {
            //todo
            dao.suspendUpdateCloudRepository(it)
            dao.suspendInsertConverterCloudRepositoryCrossRef(ConverterCloudRepositoryCrossRef(converter.converter.id, it.id))
        }
    }

    @Transaction
    suspend fun suspendDeleteConverter(converter: ConverterWithSources) {
        Log.d(TAG, "suspendDeleteConverter() $converter")
        val dao = appDatabase.appDao()
        dao.suspendDeleteConverter(converter.converter)
        dao.suspendDeleteConverterSubscriptionSourceCrossRefByConverterId(converter.converter.id)
        dao.suspendDeleteConverterCloudRepositoryCrossRefByConverterId(converter.converter.id)
        converter.cloudRepositoryList?.forEach {
            dao.suspendDeleteCloudRepository(it)
        }
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

    suspend fun suspendInsertSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().suspendInsertSubscriptionSource(source)
    }

    fun updateSubscriptionSource(source: SubscriptionSource) {
        Log.d(TAG, "updateSubscriptionSource()")
        appDatabase.appDao().updateSubscriptionSource(source)
    }

    suspend fun suspendUpdateSubscriptionSource(source: SubscriptionSource) {
        Log.d(TAG, "suspendUpdateSubscriptionSource()")
        appDatabase.appDao().suspendUpdateSubscriptionSource(source)
    }

    suspend fun suspendDeleteSubscriptionSource(source: SubscriptionSource) {
        appDatabase.appDao().suspendDeleteSubscriptionSource(source)
    }

    suspend fun suspendQuerySubscriptionSourceList() = appDatabase.appDao().suspendQuerySubscriptionSourceList()

    fun querySubscriptionSourceList() = appDatabase.appDao().querySubscriptionSourceList()

    fun querySubscriptionSourceById(sourceId: String) = appDatabase.appDao().querySubscriptionSourceById(sourceId)

    fun subscribeSubscriptionSourceList() = appDatabase.appDao().subscribeSubscriptionSourceList()


    suspend fun suspendInsertServiceAuth(auth: ServiceAuth) {
        appDatabase.appDao().suspendInsertServiceAuth(auth)
    }

    suspend fun suspendUpdateServiceAuth(auth: ServiceAuth) {
        appDatabase.appDao().suspendUpdateServiceAuth(auth)
    }

    suspend fun suspendDeleteServiceAuth(auth: ServiceAuth) {
        appDatabase.appDao().suspendDeleteServiceAuth(auth)
    }

    suspend fun suspendQueryServiceAuth(name: String): ServiceAuth? {
        return appDatabase.appDao().suspendQueryServiceAuthByName(name)
    }

    suspend fun suspendInsertCloudRepository(repository: CloudRepository) {
        appDatabase.appDao().suspendInsertCloudRepository(repository)
    }

    suspend fun suspendUpdateCloudRepository(repository: CloudRepository) {
        appDatabase.appDao().suspendUpdateCloudRepository(repository)
    }

    suspend fun suspendDeleteCloudRepository(repository: CloudRepository) {
        appDatabase.appDao().suspendDeleteCloudRepository(repository)
    }

    suspend fun suspendQueryCloudRepository(reposId: String): CloudRepository? {
        return appDatabase.appDao().suspendQueryCloudRepositoryById(reposId)
    }

}