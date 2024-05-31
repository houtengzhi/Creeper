package com.cloud.creeper.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "converter_subscription_source_cross_ref", primaryKeys = ["converter_id", "source_id"])
data class ConverterSubscriptionSourceCrossRef(@ColumnInfo("converter_id") val converterId: String,
                                               @ColumnInfo("source_id", index = true) val sourceId: String)
