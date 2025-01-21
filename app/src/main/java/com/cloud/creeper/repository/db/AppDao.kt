package com.cloud.creeper.repository.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cloud.creeper.repository.entity.CloudRepository
import com.cloud.creeper.repository.entity.Converter
import com.cloud.creeper.repository.entity.ConverterCloudRepositoryCrossRef
import com.cloud.creeper.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.entity.SubscriptionSource
import kotlinx.coroutines.flow.Flow

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertConverter(converter: Converter)

    @Update
    suspend fun suspendUpdateConverter(converter: Converter)

    @Delete
    suspend fun suspendDeleteConverter(converter: Converter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertConverterSubscriptionSourceCrossRef(crossRef: ConverterSubscriptionSourceCrossRef)

    @Update
    suspend fun suspendUpdateConverterSubscriptionSourceCrossRef(crossRef: ConverterSubscriptionSourceCrossRef)

    @Delete
    suspend fun suspendDeleteConverterSubscriptionSourceCrossRef(crossRef: ConverterSubscriptionSourceCrossRef)

    @Query("DELETE FROM converter_subscription_source_cross_ref WHERE converter_id =:converterId")
    suspend fun suspendDeleteConverterSubscriptionSourceCrossRefByConverterId(converterId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertConverterCloudRepositoryCrossRef(crossRef: ConverterCloudRepositoryCrossRef)

    @Update
    suspend fun suspendUpdateConverterCloudRepositoryCrossRef(crossRef: ConverterCloudRepositoryCrossRef)

    @Delete
    suspend fun suspendDeleteConverterCloudRepositoryCrossRef(crossRef: ConverterCloudRepositoryCrossRef)

    @Query("DELETE FROM converter_cloud_repository_cross_ref WHERE converter_id =:converterId")
    suspend fun suspendDeleteConverterCloudRepositoryCrossRefByConverterId(converterId: String)

    @Transaction
    @Query("SELECT * FROM converter WHERE name = :name")
    suspend fun suspendQueryConverterByName(name: String): ConverterWithSources?

    @Transaction
    @Query("SELECT * FROM converter WHERE name = :name")
    fun queryConverterByName(name: String): ConverterWithSources?

    @Transaction
    @Query("SELECT * FROM converter WHERE converter_id = :converterId")
    fun queryConverterById(converterId: String): ConverterWithSources?


    @Transaction
    @Query("SELECT * FROM converter")
    suspend fun queryConverterList(): List<ConverterWithSources>

    @Transaction
    @Query("SELECT * FROM converter")
    fun subscribeConverterList(): Flow<List<ConverterWithSources>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertSubscriptionSource(source: SubscriptionSource)

    @Update
    fun updateSubscriptionSource(source: SubscriptionSource)

    @Update
    suspend fun suspendUpdateSubscriptionSource(source: SubscriptionSource)

    @Delete
    fun deleteSubscriptionSource(source: SubscriptionSource)

    @Delete
    suspend fun suspendDeleteSubscriptionSource(source: SubscriptionSource)

    @Query("SELECT * FROM subscription_source")
    suspend fun suspendQuerySubscriptionSourceList(): List<SubscriptionSource>

    @Query("SELECT * FROM subscription_source")
    fun querySubscriptionSourceList(): List<SubscriptionSource>

    @Query("SELECT * FROM subscription_source WHERE source_id = :sourceId")
    fun querySubscriptionSourceById(sourceId: String): SubscriptionSource?

    @Query("SELECT * FROM subscription_source ORDER BY updated_time DESC")
    fun subscribeSubscriptionSourceList(): Flow<List<SubscriptionSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertServiceAuth(auth: ServiceAuth)

    @Update
    suspend fun suspendUpdateServiceAuth(auth: ServiceAuth)

    @Delete
    suspend fun suspendDeleteServiceAuth(auth: ServiceAuth)

    @Transaction
    @Query("SELECT * FROM service_auth WHERE service_name = :name")
    suspend fun suspendQueryServiceAuthByName(name: String): ServiceAuth?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun suspendInsertCloudRepository(repository: CloudRepository)

    @Update
    suspend fun suspendUpdateCloudRepository(repository: CloudRepository)

    @Delete
    suspend fun suspendDeleteCloudRepository(repository: CloudRepository)

    @Transaction
    @Query("SELECT * FROM cloud_repository WHERE repos_id = :reposId")
    suspend fun suspendQueryCloudRepositoryById(reposId: String): CloudRepository?
}