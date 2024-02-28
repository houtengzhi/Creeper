package com.cloud.spider.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "subscription_source")
data class SubscriptionSource(@PrimaryKey @ColumnInfo(name = "source_id") val id: String,
                              @ColumnInfo(name = "source_name") val name: String,
                              @ColumnInfo(name = "source_url") val sourceUrl: String,
                              @ColumnInfo(name = "source_type") val type: String) {
}
