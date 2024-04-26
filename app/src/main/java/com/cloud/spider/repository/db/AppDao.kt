package com.cloud.spider.repository.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cloud.spider.repository.entity.Converter
import com.cloud.spider.repository.entity.ConverterSubscriptionSourceCrossRef
import com.cloud.spider.repository.entity.ConverterWithSources
import com.cloud.spider.repository.entity.SubscriptionSource
import kotlinx.coroutines.flow.Flow

/**
 *
 * Created by cloud on 2024/2/21.
 */
@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConverter(converter: Converter)

    @Update
    suspend fun updateConverter(converter: Converter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConverterSubscriptionSourceCrossRef(crossRef: ConverterSubscriptionSourceCrossRef)

    @Update
    suspend fun updateConverterSubscriptionSourceCrossRef(crossRef: ConverterSubscriptionSourceCrossRef)

    @Transaction
    @Query("SELECT * FROM converter WHERE name = :name")
    suspend fun suspendQueryConverter(name: String): ConverterWithSources?

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
    suspend fun insertSubscriptionSource(source: SubscriptionSource)

    @Update
    suspend fun updateSubscriptionSource(source: SubscriptionSource)

    @Delete
    suspend fun deleteSubscriptionSource(source: SubscriptionSource)

    @Query("SELECT * FROM subscription_source")
    suspend fun querySubscriptionSourceList(): List<SubscriptionSource>

    @Query("SELECT * FROM subscription_source ORDER BY updated_time DESC")
    fun subscribeSubscriptionSourceList(): Flow<List<SubscriptionSource>>
}