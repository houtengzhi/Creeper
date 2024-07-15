package com.cloud.creeper.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 *
 * Created by cloud on 2024/7/12.
 */
@Entity(tableName = "converter_cloud_repository_cross_ref", primaryKeys = ["converter_id", "repos_id"])
class ConverterCloudRepositoryCrossRef(@ColumnInfo("converter_id") val converterId: String,
                                       @ColumnInfo("repos_id", index = true) val reposId: String) {
}